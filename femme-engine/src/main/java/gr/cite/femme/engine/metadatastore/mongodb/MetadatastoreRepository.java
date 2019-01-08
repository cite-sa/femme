package gr.cite.femme.engine.metadatastore.mongodb;

public interface MetadatastoreRepository extends MongoMetadataCollection {
	void close();
}
