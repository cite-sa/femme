package gr.cite.femme.metadata.xpath;

import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.commons.metadata.analyzer.core.MetadataSchemaAnalysis;
import gr.cite.commons.metadata.analyzer.json.JSONSchemaAnalyzer;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.datastores.MetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.datastores.MetadataSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.elasticsearch.ElasticMetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.mongodb.MongoMetadataSchemaIndexDatastore;
import gr.cite.femme.model.Metadatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataXPath {

    private static final Logger logger = LoggerFactory.getLogger(MetadataXPath.class);

    /*private MongoMetadataAndSchemaIndexDatastore xPathDatastore;*/
    private MetadataIndexDatastore metadataIndexDatastore;
    private MetadataSchemaIndexDatastore metadataSchemaIndexDatastore;

    /*public MetadataXPath() {
        xPathDatastore = new MongoMetadataAndSchemaIndexDatastore();
    }*/

    public MetadataXPath() throws UnknownHostException {
        this.metadataIndexDatastore = new ElasticMetadataIndexDatastore();
        this.metadataSchemaIndexDatastore = new MongoMetadataSchemaIndexDatastore();
    }

    public MetadataXPath(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore, MetadataIndexDatastore metadataIndexDatastore) {
        this.metadataSchemaIndexDatastore = metadataSchemaIndexDatastore;
        this.metadataIndexDatastore = metadataIndexDatastore;
    }

    public void close() throws IOException {
        metadataIndexDatastore.close();
        metadataSchemaIndexDatastore.close();
    }

    public void index(Metadatum metadatum) throws IOException, UnsupportedOperationException, MetadataIndexException, HashGenerationException {
        String metadatumJson;
        if (MediaType.APPLICATION_XML.equals(metadatum.getContentType()) || MediaType.TEXT_XML.equals(metadatum.getContentType())) {
            metadatumJson = XmlJsonConverter.xmlToJson(metadatum.getValue());
        } else {
            throw new UnsupportedOperationException("Metadata indexing is not yet supported for media type " + metadatum.getContentType().toString());
        }

        /*List<MaterializedPathsNode> nodes = PathMaterializer.materialize(metadatum.getId(), metadatumJson);
        xPathDatastore.insertMany(nodes);*/

        /*MetadataSchemaAnalysis metadataSchemaAnalysis = JSONSchemaAnalyzer.analyze(metadatumJson);*/
        MetadataSchema metadataSchema = new MetadataSchema(JSONSchemaAnalyzer.analyze(metadatumJson));

        /*metadataSchema.setSchema(metadataSchemaAnalysis.getSchema());
        metadataSchema.setHash(metadataSchemaAnalysis.hash());*/

        metadataSchemaIndexDatastore.indexSchema(metadataSchema);

        IndexableMetadatum indexableMetadatum = new IndexableMetadatum();
        indexableMetadatum.setMetadataSchemaId(metadataSchema.getId());
        indexableMetadatum.setMetadatumId(metadatum.getId());
        indexableMetadatum.setElementId(metadatum.getElementId());
        indexableMetadatum.setOriginalContentType(metadatum.getContentType());
        indexableMetadatum.setValue(metadatumJson);
        metadataIndexDatastore.indexMetadatum(indexableMetadatum, metadataSchema);

    }

    public List<Metadatum> xPath(String xPath) throws MetadataIndexException {
        /*MongoXPathQuery xPathQuery = new MongoXPathQuery(xPathDatastore);*/
        /*MongoXPathResult xPathResult = xPathQuery.query(xPath);*/
        /*Bson mongoQuery = xPathQuery.query(xPath);
        logger.info(mongoQuery.toString());*/


        /*List<MetadataSchema> paths = metadataSchemaIndexDatastore.findMetadataIndexPath("wcs:CoverageDescription");*/
        List<MetadataSchema> paths = metadataSchemaIndexDatastore.findMetadataIndexPath("boundedBy");
        /*paths = metadataSchemaIndexDatastore.findMetadataIndexPath("RectifiedGrid$");*/

        List<IndexableMetadatum> xPathResult = metadataIndexDatastore.query("\"value.wcs:CoverageDescriptions.ns.wcs.keyword\" : \"http://www.opengis.net/wcs/2.0\"");
        List<Metadatum> metadata = xPathResult.stream().map(indexableMetadatum -> {
            Metadatum metadatum = new Metadatum();
            metadatum.setId(indexableMetadatum.getMetadatumId());
            return metadatum;
        }).collect(Collectors.toList());

        return metadata;

    }
}
