package gr.cite.femme.metadata.xpath.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoXPathDatastore {

    private static final Logger logger = LoggerFactory.getLogger(MongoXPathDatastore.class);

    private MongoXPathDatastoreClient mongoClient;

    private MongoCollection<MaterializedPathsNode> materializedPaths;

    public MongoXPathDatastore() {
        mongoClient = new MongoXPathDatastoreClient();
        materializedPaths = mongoClient.getMaterializedPaths();
    }

    public MongoXPathDatastore(String dbHost, String dbName, String collectionName) {
        mongoClient = new MongoXPathDatastoreClient(dbHost, dbName, collectionName);
        materializedPaths = mongoClient.getMaterializedPaths();
    }

    /*public MongoCollection<MaterializedPathsNode> getMaterializedPaths() {
        return materializedPaths;
    }*/

    public void close() {
        mongoClient.close();
    }

    public void insertMany(List<MaterializedPathsNode> materializedPathsNodes) {
        materializedPaths.insertMany(materializedPathsNodes);
    }

    public List<MaterializedPathsNode> xPath(Bson query) {
//        List<MaterializedPathsNode> results = new ArrayList<>();
//        materializedPaths.find(query).into(results);

        List<MaterializedPathsNode> elements = new ArrayList<>();
        FindIterable<MaterializedPathsNode> results = materializedPaths.find(query);
//        MongoCursor<MaterializedPathsNode> cursor = results.iterator();

        results.into(elements);
        /*try (MongoCursor<MaterializedPathsNode> cursor = results.iterator()) {
            while (cursor.hasNext()) {
                MaterializedPathsNode element = cursor.next();
                elements.add(element);
            }
        }*//* finally {
            cursor.close();
        }*/

        return elements;
    }
}
