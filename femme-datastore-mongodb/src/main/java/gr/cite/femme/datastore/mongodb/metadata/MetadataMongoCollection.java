package gr.cite.femme.datastore.mongodb.metadata;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.InsertOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJson;

public class MetadataMongoCollection implements MetadataStore {

	MongoCollection<MetadatumJson> metadataCollection;

	public MetadataMongoCollection() {

	}

	public MetadataMongoCollection(MongoCollection<MetadatumJson> metadataCollection) {
		this.metadataCollection = metadataCollection;
	}

	@Override
	public String insert(Metadatum metadatum) throws MetadataStoreException {
		MetadatumJson metadatumJson = new MetadatumJson(metadatum);
		metadataCollection.insertOne(metadatumJson);
		return metadatumJson.getId();
	}

	@Override
	public Metadatum get(String metadatumId) throws MetadataStoreException {
		MetadatumJson metadatumJson = metadataCollection.find(Filters.eq("_id", metadatumId)).limit(1).first();
		return new Metadatum(metadatumJson.getId(), metadatumJson.getElementId(), metadatumJson.getName(),
				metadatumJson.getValue(), metadatumJson.getContentType());
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
	public <T extends Element> List<T> find(List<T> elementIds, String xPath) throws MetadataStoreException {
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
	public Metadatum xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Metadatum metadatum) throws MetadataStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub

	}
}
