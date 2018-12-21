package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.commons.metadata.analyzer.json.JSONSchemaAnalyzer;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.engine.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.core.model.Metadatum;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class ElasticReindexingProcess implements ReIndexingProcess {

	private static final ObjectMapper mapper = new ObjectMapper();

	private MetadataSchemaIndexDatastore metadataSchemaIndexDatastore;
	private ElasticMetadataIndexDatastore elasticMetadataIndexDatastore;
	private ElasticMetadataIndexDatastoreRepository indexClient;

	private UUID indexProcessId;
	private Indices indices;

	private Instant startTime;
	private Instant endTime;

	private boolean reIndexingInProgress;

	private LongAdder total = new LongAdder();

	public ElasticReindexingProcess(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore, ElasticMetadataIndexDatastore elasticMetadataIndexDatastore, ElasticMetadataIndexDatastoreRepository indexClient) {
		this.metadataSchemaIndexDatastore = metadataSchemaIndexDatastore;
		this.elasticMetadataIndexDatastore = elasticMetadataIndexDatastore;
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
		try {
			this.indexClient.swapWithAliasOldIndices(this.indices.getIndicesInfo().entrySet().stream()
					.map(entry -> this.indexClient.getFullIndexName(entry.getKey(), entry.getValue())).collect(Collectors.toSet()));
			//this.indexClient.swapWithAliasOldIndices(this.elasticMetadataIndexDatastore.getIndices().getFullIndexNames(), this.indices.getFullIndexNames());
		} finally {
			this.endTime = Instant.now();
			this.reIndexingInProgress = false;
		}
		this.elasticMetadataIndexDatastore.setIndices(this.indices);
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
			try {
				metadatumJson = XmlJsonConverter.xmlToFemmeJson(metadatum.getValue());
			} catch (XMLStreamException e) {
				throw new MetadataIndexException(e.getMessage(), e);
			}
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
		String metadataSchemaId = metadataSchema.getId();
		String randomId = UUID.randomUUID().toString();
		String uniqueMetadataIndexName;
		if (this.indices.compareAndAdd(metadataSchemaId, randomId)) {
			uniqueMetadataIndexName = this.indexClient.getFullIndexName(metadataSchemaId, randomId);
			this.indexClient.createIndex(uniqueMetadataIndexName);
			this.indexClient.createMapping(metadataSchema, uniqueMetadataIndexName);
		} else {
			uniqueMetadataIndexName = this.indexClient.getFullIndexName(metadataSchemaId, this.indices.getUniqueId(metadataSchemaId));
		}

		String indexableMetadatumSerialized;
		try {
			indexableMetadatumSerialized = mapper.writeValueAsString(indexableMetadatum);
		} catch (JsonProcessingException e) {
			throw new MetadataIndexException("Metadatum serialization failed", e);
		}
		this.indexClient.insert(indexableMetadatumSerialized, uniqueMetadataIndexName);

	}
}
