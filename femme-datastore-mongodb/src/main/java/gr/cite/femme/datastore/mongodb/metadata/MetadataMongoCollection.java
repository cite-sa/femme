package gr.cite.femme.datastore.mongodb.metadata;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJson;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;

public class MetadataMongoCollection implements MetadataStore {

	private MongoCollection<MetadatumJson> metadataCollection;
	
	private static Function<MetadatumJson, Metadatum> metadatumTransformation = metadatumJson -> {
		Metadatum metadatum = new Metadatum();
		metadatum.setId(metadatumJson.getId());
		metadatum.setElementId(metadatumJson.getElementId());
		metadatum.setName(metadatumJson.getName());
		metadatum.setContentType(metadatumJson.getContentType());
		metadatum.setValue(metadatumJson.getValue());
		return metadatum;
	};

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
		MetadatumJson metadatumJson = metadataCollection.find(Filters.eq(FieldNames.ID, metadatumId)).limit(1).first();
		return new Metadatum(metadatumJson.getId(), metadatumJson.getElementId(), metadatumJson.getName(),
				metadatumJson.getValue(), metadatumJson.getContentType());
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return metadataCollection.find(Filters.eq(FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
				.map(metadatumTransformation).into(new ArrayList<>());
	}
	
	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Element> T find(T element, String xPath) throws MetadataStoreException {
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
