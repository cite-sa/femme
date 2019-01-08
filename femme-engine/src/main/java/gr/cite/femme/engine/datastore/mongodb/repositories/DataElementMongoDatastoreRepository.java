package gr.cite.femme.engine.datastore.mongodb.repositories;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.engine.datastore.mongodb.codecs.DataElementCodecProvider;
import gr.cite.femme.engine.datastore.mongodb.codecs.SystemicMetadataCodecProvider;
import gr.cite.femme.core.model.FieldNames;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataElementMongoDatastoreRepository extends MongoDatastoreRepository<DataElement> {
	private static final Logger logger = LoggerFactory.getLogger(DataElementMongoDatastoreRepository.class);
	private static final String COLLECTION_NAME = "dataElements";
	
	public DataElementMongoDatastoreRepository(String[] hosts, String name, boolean sharding) {
		super(hosts, name, sharding);
		
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
			MongoClient.getDefaultCodecRegistry(),
			CodecRegistries.fromProviders(
				new DataElementCodecProvider(),
				new SystemicMetadataCodecProvider()),
			MongoClient.getDefaultCodecRegistry());
		
		setCollection(this.getDatabase().getCollection(COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry));
		
		createIndexes(this.getCollection());
	}
	
	protected void createIndexes(MongoCollection<DataElement> dataElements) {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		//this.database.getRepository(MongoDatastoreRepository.DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.ENDPOINT), uniqueIndexOptions);
		/*this.database.getRepository(MongoDatastoreRepository.DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.compoundIndex(
				Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.DATA_ELEMENT_COLLECTION_ENDPOINT)), uniqueIndexOptions);*/
		dataElements.createIndex(Indexes.compoundIndex(Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.COLLECTIONS)), uniqueIndexOptions);
	}
}
