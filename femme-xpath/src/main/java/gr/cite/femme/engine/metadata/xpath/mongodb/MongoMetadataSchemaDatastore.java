package gr.cite.femme.engine.metadata.xpath.mongodb;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MongoMetadataSchemaDatastore implements MetadataSchemaIndexDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataSchemaDatastore.class);

	private MongoMetadataSchemaDatastoreRepository metadataSchemaDatastoreRepository;

	public MongoMetadataSchemaDatastore(MongoMetadataSchemaDatastoreRepository metadataSchemaDatastoreRepository) {
		this.metadataSchemaDatastoreRepository = metadataSchemaDatastoreRepository;
	}

	@Override
	public void index(MetadataSchema schema) {
		MetadataSchema existingSchema = this.metadataSchemaDatastoreRepository.get(Filters.eq("checksum", schema.getChecksum()));
		//MetadataSchema existingSchema = this.schemasCollection.find(Filters.eq("checksum", schema.getChecksum())).limit(1).first();
		if (existingSchema == null) {
			try {
				this.metadataSchemaDatastoreRepository.insert(schema);
			} catch (MetadataStoreException e) {
				logger.error("Error inserting metadata schema", e);
			}
			//this.schemasCollection.insertOne(schema);
		} else {
			schema.setId(existingSchema.getId());
		}
	}

	@Override
	public List<MetadataSchema> findMetadataIndexPath(String regex) {
		Bson filterByRegex = Aggregates.match(Filters.regex("schema.path", regex));
		/*return this.schemasCollection.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				//Aggregates.project(new Document().append("_id", 0).append("schema", 1)),
				//Aggregates.group(null, Accumulators.addToSet("ids", "$_id"), Accumulators.addToSet("schema", "$schema"))
				Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		return this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
	}

	@Override
	public Map<String, List<String>> findMetadataIndexPathByRegexAndGroupById(String regex) {
		//List<MetadataSchema> metadataSchemas = new ArrayList<>();
		long start = System.currentTimeMillis();
		
		Bson filterByRegex = Aggregates.match(Filters.regex("schema.path", regex));
		
		Map<String, List<String>> pathAndMetadataSchemaIds = new ConcurrentHashMap<>();
		
		this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			filterByRegex,
			Aggregates.unwind("$schema"),
			filterByRegex,
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).forEach((Block<MetadataSchema>) metadataSchema -> addMetadataSchemaToMap(metadataSchema, pathAndMetadataSchemaIds));
		
		long end = System.currentTimeMillis();
		logger.info("findMetadataIndexPathByRegexAndGroupById query time: " + (end - start) + " ms");

		return pathAndMetadataSchemaIds;
	}
	
	private void addMetadataSchemaToMap(MetadataSchema metadataSchema, Map<String, List<String>> pathAndMetadataSchemaIds) {
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
	}

	@Override
	public List<MetadataSchema> findArrayMetadataIndexPaths() {
		/*return this.schemasCollection.aggregate(Arrays.asList(
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.unwind("$schema"),
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		return this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			Aggregates.match(new Document().append("schema.array", true)),
			Aggregates.unwind("$schema"),
			Aggregates.match(new Document().append("schema.array", true)),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
	}
	
	@Override
	public List<MetadataSchema> findArrayMetadataIndexPaths(List<String> ids, String pathPrefix) {
		long start = System.currentTimeMillis();
		
		List<ObjectId> objectIds = ids.stream().map(ObjectId::new).collect(Collectors.toList());
		
		/*return this.schemasCollection.aggregate(Arrays.asList(
			Aggregates.match(Filters.and(Filters.eq("schema.array", true), Filters.in("_id", objectIds))),
			Aggregates.unwind("$schema"),
			Aggregates.match(Filters.and(Filters.eq("schema.array", true))),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		List<MetadataSchema> schemas = this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			Aggregates.match(Filters.and(Filters.eq("schema.array", true), Filters.in("_id", objectIds))),
			Aggregates.unwind("$schema"),
			Aggregates.match(Filters.and(Filters.eq("schema.array", true))),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
		
		long end = System.currentTimeMillis();
		logger.info("findArrayMetadataIndexPaths query time: " + (end - start) + " ms");
		
		return schemas;
	}

	@Override
	public List<MetadataSchema> findArrayMetadataIndexPathsByRegex(String regex) {
		Bson filterByRegex = Aggregates.match(Filters.and(Filters.eq("schema.array", true), Filters.regex("schema.path", regex)));
		
		/*return this.schemasCollection.aggregate(Arrays.asList(
				filterByRegex,
				Aggregates.unwind("$schema"),
				filterByRegex,
				Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		return this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			filterByRegex,
			Aggregates.unwind("$schema"),
			filterByRegex,
			Aggregates.group(null, Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
	}

	//@Override
	public List<MetadataSchema> findArrayMetadataIndexPathsAndGroupById() {
		/*return this.schemasCollection.aggregate(Arrays.asList(
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.unwind("$schema"),
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		return this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			Aggregates.match(new Document().append("schema.array", true)),
			Aggregates.unwind("$schema"),
			Aggregates.match(new Document().append("schema.array", true)),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
	}

	@Override
	public List<MetadataSchema> findArrayMetadataIndexPaths(String id) {
		/*return this.schemasCollection.aggregate(Arrays.asList(
				Aggregates.match(Filters.and(Filters.eq("_id", new ObjectId(id)), new Document().append("schema.array", true))),
				Aggregates.unwind("$schema"),
				Aggregates.match(new Document().append("schema.array", true)),
				Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());*/
		
		return this.metadataSchemaDatastoreRepository.aggregate(Arrays.asList(
			Aggregates.match(Filters.and(Filters.eq("_id", new ObjectId(id)), new Document().append("schema.array", true))),
			Aggregates.unwind("$schema"),
			Aggregates.match(new Document().append("schema.array", true)),
			Aggregates.group("$_id", Accumulators.addToSet("schema", "$schema"))
		)).into(new ArrayList<>());
	}
}


