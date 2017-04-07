package gr.cite.femme.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.commons.metadata.analyzer.json.JSONSchemaAnalyzer;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.model.Metadatum;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class ElasticReindexingProcess implements ReIndexingProcess {

	private static final ObjectMapper mapper = new ObjectMapper();

	private MetadataSchemaIndexDatastore metadataSchemaIndexDatastore;
	private ElasticMetadataIndexDatastoreClient indexClient;

	private UUID indexProcessId;
	private Indices indices;

	private Instant startTime;
	private Instant endTime;

	private boolean reIndexingInProgress;

	private LongAdder total = new LongAdder();

	public ElasticReindexingProcess(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore, ElasticMetadataIndexDatastoreClient indexClient) {
		this.metadataSchemaIndexDatastore = metadataSchemaIndexDatastore;
		this.indexClient = indexClient;
		this.reIndexingInProgress = false;
	}

	@Override
	public synchronized void begin() throws MetadataIndexException {
		this.indexProcessId = UUID.randomUUID();
		this.indices = new Indices();
		this.startTime = Instant.now();
		this.reIndexingInProgress = true;
	}

	@Override
	public synchronized void end() throws MetadataIndexException {
		this.indexClient.swapWithAliasOldIndices(this.indices.get().stream().map(index -> index + "_" + this.indexProcessId).collect(Collectors.toSet()));
		this.endTime = Instant.now();
		this.indices = null;
		this.reIndexingInProgress = false;
	}

	@Override
	public synchronized boolean reIndexingInProgress() {
		return this.reIndexingInProgress;
	}

	@Override
	public void index(Metadatum metadatum) throws UnsupportedOperationException, MetadataIndexException {
		String metadatumJson;
		if (MediaType.APPLICATION_XML.equals(metadatum.getContentType()) || MediaType.TEXT_XML.equals(metadatum.getContentType())) {
			metadatumJson = XmlJsonConverter.xmlToJson(metadatum.getValue());
		} else {
			throw new UnsupportedOperationException("Metadata indexing is not yet supported for media type " + metadatum.getContentType());
		}

        /*List<MaterializedPathsNode> nodes = PathMaterializer.materialize(metadatum.getId(), metadatumJson);
        xPathDatastore.insertMany(nodes);*/

		MetadataSchema metadataSchema;
		try {
			metadataSchema = new MetadataSchema(JSONSchemaAnalyzer.analyze(metadatumJson));
		} catch (HashGenerationException | IOException e) {
			throw new MetadataIndexException("Metadata schema analysis failed", e);
		}
		this.metadataSchemaIndexDatastore.index(metadataSchema);

		IndexableMetadatum indexableMetadatum = new IndexableMetadatum();
		indexableMetadatum.setMetadataSchemaId(metadataSchema.getId());
		indexableMetadatum.setMetadatumId(metadatum.getId());
		indexableMetadatum.setElementId(metadatum.getElementId());
		indexableMetadatum.setOriginalContentType(metadatum.getContentType());
		indexableMetadatum.setValue(metadatumJson);
		indexableMetadatum.setCreated(metadatum.getSystemicMetadata().getCreated());
		indexableMetadatum.setModified(metadatum.getSystemicMetadata().getModified());

		index(indexableMetadatum, metadataSchema);

		this.total.increment();
	}

	private void index(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		String indexName = this.indexClient.getIndexAlias() + "_" + indexableMetadatum.getMetadataSchemaId();
		String timestampedMetadataIndexName = indexName + "_" + this.indexProcessId;
		if (this.indices.compareAndAdd(indexName)) {
			this.indexClient.createIndex(timestampedMetadataIndexName);
			this.indexClient.createMapping(metadataSchema, timestampedMetadataIndexName);
		}

		/*if (!this.indexClient.mappingExists(timestampedMetadataIndexName)) {
			this.indexClient.createMapping(metadataSchema, this.indexName);
		}*/

		String indexableMetadatumSerialized;
		try {
			indexableMetadatumSerialized = mapper.writeValueAsString(indexableMetadatum);
		} catch (JsonProcessingException e) {
			throw new MetadataIndexException("Metadatum serialization failed", e);
		}
		this.indexClient.insert(indexableMetadatumSerialized, timestampedMetadataIndexName);

	}
}
