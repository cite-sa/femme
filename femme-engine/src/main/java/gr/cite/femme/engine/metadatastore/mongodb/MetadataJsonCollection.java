package gr.cite.femme.engine.metadatastore.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoCursor;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import org.bson.types.ObjectId;

import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.cite.femme.engine.datastore.mongodb.codecs.MetadatumJson;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.exceptions.MetadataStoreException;

public class MetadataJsonCollection implements MongoMetadataCollection {

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

	public MetadataJsonCollection() {

	}

	public MetadataJsonCollection(MongoCollection<MetadatumJson> metadataCollection) {
		this.metadataCollection = metadataCollection;
	}

	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException {
		MetadatumJson metadatumJson = new MetadatumJson(metadatum);
		metadataCollection.insertOne(metadatumJson);
	}

	@Override
	public void update(Metadatum metadatum) throws MetadataStoreException {

	}

	@Override
	public void update(String id, Map<String, Object> fieldsAndValues) throws MetadataStoreException {

	}

	@Override
	public void updateStatus(String id, Status status) throws MetadataStoreException {

	}

	@Override
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException {
		MetadatumJson metadatumJson = metadataCollection.find(Filters.eq(FieldNames.ID, metadatum.getId())).limit(1).first();
		return new Metadatum(metadatumJson.getId(), metadatumJson.getElementId(), metadatumJson.getName(),
				metadatumJson.getValue(), metadatumJson.getContentType());
	}

	@Override
	public MongoCursor<Metadatum> findAll(boolean lazy) throws MetadataStoreException {
		return null;
	}

	@Override
	public MongoCursor<Metadatum> findAllBeforeTimestamp(Instant timestamp) throws MetadataStoreException {
		return null;
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return metadataCollection.find(Filters.eq(FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
				.map(metadatumTransformation).into(new ArrayList<>());
	}
	
	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		return metadataCollection.find(Filters.eq(FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
				.map(metadatumTransformation).into(new ArrayList<>());
	}

	@Override
	public void delete(Metadatum metadatum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll(String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
