package gr.cite.femme.metadata.xpath.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.datastores.MetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.datastores.MetadataSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoMetadataAndSchemaIndexDatastore implements MetadataSchemaIndexDatastore, MetadataIndexDatastore {

    private static final Logger logger = LoggerFactory.getLogger(MongoMetadataAndSchemaIndexDatastore.class);

    private MongoMetadataIndexDatastoreClient mongoClient;

//    private MongoCollection<MaterializedPathsNode> materializedPaths;

    private MongoCollection<IndexableMetadatum> metadataCollection;

    private MongoCollection<MetadataSchema> schemasCollection;

    public MongoMetadataAndSchemaIndexDatastore() {
        mongoClient = new MongoMetadataIndexDatastoreClient();
        //materializedPaths = mongoClient.getMaterializedPaths();
        metadataCollection = mongoClient.getMetadataCollection();
        schemasCollection = mongoClient.getSchemasCollection();
    }

    public MongoMetadataAndSchemaIndexDatastore(String dbHost, String dbName, String transformedMetadataCollectionName, String schemasCollectionName) {
        mongoClient = new MongoMetadataIndexDatastoreClient(dbHost, dbName, transformedMetadataCollectionName, schemasCollectionName);
        //materializedPaths = mongoClient.getMaterializedPaths();
        metadataCollection = mongoClient.getMetadataCollection();
        schemasCollection = mongoClient.getSchemasCollection();
    }

    public void close() {
        mongoClient.close();
    }

    @Override
    public void indexSchema(MetadataSchema schema) {
        List<MetadataSchema> existingSchema = new ArrayList<>();
        schemasCollection.find(Filters.eq("hash", schema.getHash())).into(existingSchema);
        if (existingSchema.size() == 0) {
            schemasCollection.insertOne(schema);
        }
    }

    @Override
    public List<MetadataSchema> findMetadataIndexPath(String regex) {
        List<MetadataSchema> metadataSchemas = new ArrayList<>();
        Bson filterByRegex = Aggregates.match(Filters.regex("schema", regex));
        this.schemasCollection.aggregate(Arrays.asList(
                filterByRegex,
                Aggregates.unwind("$schema"),
                filterByRegex,
                //Aggregates.project(new Document().append("_id", 0).append("schema", 1)),
                Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
        )).into(metadataSchemas);

        return metadataSchemas;
    }

    @Override
    public List<MetadataSchema> findArrayMetadataIndexPaths(String id) {
        List<MetadataSchema> metadataSchemas = new ArrayList<>();

        this.schemasCollection.aggregate(Arrays.asList(
                Aggregates.match(Filters.and(Filters.eq("_id", new ObjectId(id)), new Document().append("schema.array", true))),
                Aggregates.unwind("$schema"),
                Aggregates.match(new Document().append("schema.array", true)),
                Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
        )).into(metadataSchemas);

        return metadataSchemas;
    }

    @Override
    public void indexMetadatum(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) {
        metadataCollection.insertOne(indexableMetadatum);
    }

    @Override
    public List<IndexableMetadatum> query(String query) throws MetadataIndexException {
        List<IndexableMetadatum> results = new ArrayList<>();
        metadataCollection.find(Document.parse(query)).into(results);
        return results;
    }



    /*public List<IndexableMetadatum> findMetadata(Bson query) {
        List<IndexableMetadatum> metadata = new ArrayList<>();
        metadataCollection.find(query).into(metadata);
        return metadata;
    }

    public void insertMany(List<MaterializedPathsNode> materializedPathsNodes) {
        materializedPaths.insertMany(materializedPathsNodes);
    }

    public List<MaterializedPathsNode> xPath(Bson query) {

        List<MaterializedPathsNode> elements = new ArrayList<>();
        FindIterable<MaterializedPathsNode> results = materializedPaths.find(query);

        results.into(elements);
        return elements;
    }*/
}
