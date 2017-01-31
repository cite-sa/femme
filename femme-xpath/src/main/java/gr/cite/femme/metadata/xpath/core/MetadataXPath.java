package gr.cite.femme.metadata.xpath.core;

import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.femme.metadata.xpath.mongodb.MongoXPathDatastore;
import gr.cite.femme.metadata.xpath.transformation.PathMaterializer;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoXPathQuery;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoXPathResult;
import gr.cite.femme.model.Metadatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataXPath {

    private static final Logger logger = LoggerFactory.getLogger(MetadataXPath.class);

    private MongoXPathDatastore xPathDatastore;

    public MetadataXPath() {
        xPathDatastore = new MongoXPathDatastore();
    }

    public MetadataXPath(String host, String db, String collectionName) {
        xPathDatastore = new MongoXPathDatastore(host, db, collectionName);
    }

    public void close() {
        xPathDatastore.close();
    }

    public void index(Metadatum metadatum) throws IOException, UnsupportedOperationException {
        String metadatumJson = null;
        if (MediaType.APPLICATION_XML.equals(metadatum.getContentType()) || MediaType.TEXT_XML.equals(metadatum.getContentType())) {
            metadatumJson = XmlJsonConverter.xmlToJson(metadatum.getValue());
        } else {
            throw new UnsupportedOperationException("Metadata indexing is not yet supported for media type " + metadatum.getContentType().toString());
        }

//        logger.info(metadatum.getId());
        List<MaterializedPathsNode> nodes = PathMaterializer.materialize(metadatum.getId(), metadatumJson);
        xPathDatastore.insertMany(nodes);
    }

    public List<Metadatum> xPath(String xPath) {
        MongoXPathQuery xPathQuery = new MongoXPathQuery(xPathDatastore);
        MongoXPathResult xPathResult = xPathQuery.query(xPath);

        List<Metadatum> metadata = xPathResult.getResult().stream().map(node -> {
            Metadatum metadatum = new Metadatum();
            metadatum.setId(node.getMetadatumId());
            return metadatum;
        }).collect(Collectors.toList());

        return metadata;
    }
}
