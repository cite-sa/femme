package gr.cite.femme.engine.datastore.mongodb.repositories;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.engine.datastore.mongodb.codecs.CollectionCodecProvider;
import gr.cite.femme.engine.datastore.mongodb.codecs.SystemicMetadataCodecProvider;
import gr.cite.femme.core.model.FieldNames;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionMongoDatastoreRepository extends MongoDatastoreRepository<Collection> {
	private static final Logger logger = LoggerFactory.getLogger(CollectionMongoDatastoreRepository.class);
	private static final String COLLECTION_NAME = "collections";
	
	public CollectionMongoDatastoreRepository(String[] hosts, String name, boolean sharding) {
		super(hosts, name, sharding);
		
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
			MongoClient.getDefaultCodecRegistry(),
			CodecRegistries.fromProviders(
				new CollectionCodecProvider(),
				new SystemicMetadataCodecProvider()),
			MongoClient.getDefaultCodecRegistry());
		
		setCollection(this.getDatabase().getCollection(COLLECTION_NAME, Collection.class).withCodecRegistry(codecRegistry));
		//this.dataElements = this.database.getRepository(MongoDatastoreRepository.DATA_ELEMENTS_COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry);
		
		createIndexes(this.getCollection());
	}
	
	protected void createIndexes(MongoCollection<Collection> collections) {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		collections.createIndex(Indexes.ascending(FieldNames.ENDPOINT, FieldNames.NAME), uniqueIndexOptions);
	}
}
