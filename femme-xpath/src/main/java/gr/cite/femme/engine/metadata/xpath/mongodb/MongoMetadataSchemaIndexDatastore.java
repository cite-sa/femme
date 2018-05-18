package gr.cite.femme.engine.metadata.xpath.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MongoMetadataSchemaIndexDatastore implements MetadataSchemaIndexDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataSchemaIndexDatastore.class);

	private MongoMetadataIndexDatastoreClient mongoClient;
	private MongoCollection<MetadataSchema> schemasCollection;

	public MongoMetadataSchemaIndexDatastore() {
		this.mongoClient = new MongoMetadataIndexDatastoreClient();
		this.schemasCollection = mongoClient.getSchemasCollection();
	}

	public MongoMetadataSchemaIndexDatastore(String host, int port, String name) {
		this.mongoClient = new MongoMetadataIndexDatastoreClient(host, port, name);
		this.schemasCollection = mongoClient.getSchemasCollection();
	}

	public void close() {
		mongoClient.close();
	}

	@Override
	public void index(MetadataSchema schema) {
		MetadataSchema existingSchema = this.schemasCollection.find(Filters.eq("checksum", schema.getChecksum())).limit(1).first();
		if (existingSchema == null) {
			this.schemasCollection.insertOne(schema);
		} else {
			schema.setId(existingSchema.getId());
		}
	}

	/*public List<String> findSuperSchemas(MetadataSchema metadataSchema) {
		metadataSchema.getSchema().removeIf(jsonPath -> !jsonPath.isArray());
		findArrayMetadataIndexPathsAndGroupById().forEach(metadataSchemaWithArrays -> {

		});

		return null;
	}*/

	@Override
	public List<MetadataSchema> findMetadataIndexPath(String regex) {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();
		Bson filterByRegex = Aggregates.match(Filters.regex("schema.path", regex));
		this.schemasCollection.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				//Aggregates.project(new Document().append("_id", 0).append("schema", 1)),
				//Aggregates.group(null, Accumulators.addToSet("ids", "$_id"), Accumulators.addToSet("schema", "$schema"))
				Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);

		return metadataSchemas;
	}

	@Override
	public Map<String, List<String>> findMetadataIndexPathByRegexAndGroupById(String regex) {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();
		Bson filterByRegex = Aggregates.match(Filters.regex("schema.path", regex));
		this.schemasCollection.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);

		Map<String, List<String>> pathAndMetadataSchemaIds = new ConcurrentHashMap<>();

		metadataSchemas.forEach(metadataSchema -> {
			final String metadataSchemaId = metadataSchema.getId();
			metadataSchema.getSchema().forEach(schema -> {
				if (pathAndMetadataSchemaIds.containsKey(schema.getPath())) {
					pathAndMetadataSchemaIds.get(schema.getPath()).add(metadataSchemaId);
				} else {
					List<String> metadataSchemasIds = new ArrayList<>();
					metadataSchemasIds.add(metadataSchemaId);
					pathAndMetadataSchemaIds.put(schema.getPath(), metadataSchemasIds);
				}
			});
		});

		return pathAndMetadataSchemaIds;
	}

	@Override
	public List<MetadataSchema> findArrayMetadataIndexPaths() {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();

		this.schemasCollection.aggregate(Arrays.asList(
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.unwind("$schema"),
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);

		return metadataSchemas;
	}
	
	@Override
	public List<MetadataSchema> findArrayMetadataIndexPaths(List<String> ids, String pathPrefix) {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();
		List<ObjectId> objectIds = ids.stream().map(ObjectId::new).collect(Collectors.toList());
		
		this.schemasCollection.aggregate(Arrays.asList(
			Aggregates.match(Filters.and(Filters.eq("schema.array", true), Filters.in("_id", objectIds))),
			Aggregates.unwind("$schema"),
			Aggregates.match(Filters.and(Filters.eq("schema.array", true))),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);
		
		//Filters.regex("schema.path", "^" + pathPrefix + ".*$")
		
		return metadataSchemas;
	}

	@Override
	public List<MetadataSchema> findArrayMetadataIndexPathsByRegex(String regex) {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();
		Bson filterByRegex = Aggregates.match(Filters.and(Filters.eq("schema.array", true), Filters.regex("schema.path", regex)));
		this.schemasCollection.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);

		return metadataSchemas;
	}

	//@Override
	public List<MetadataSchema> findArrayMetadataIndexPathsAndGroupById() {
		List<MetadataSchema> metadataSchemas = new ArrayList<>();

		this.schemasCollection.aggregate(Arrays.asList(
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.unwind("$schema"),
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
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
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(metadataSchemas);

		return metadataSchemas;
	}
}


