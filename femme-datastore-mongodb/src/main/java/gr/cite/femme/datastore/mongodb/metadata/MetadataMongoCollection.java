package gr.cite.femme.datastore.mongodb.metadata;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;

public class MetadataMongoCollection implements MetadataStore {
	
	MongoCollection<Metadatum> metadata;
	
	public MetadataMongoCollection() {
		
	}
	
	public MetadataMongoCollection(MongoCollection<Metadatum> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public ObjectId insert(Metadatum metadatum, String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadatum get(String fileId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Element> List<T> find(List<T> elementIds, String xPath) throws MetadataStoreException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(Metadatum metadatum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(List<Metadatum> metadataList) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Metadatum xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
