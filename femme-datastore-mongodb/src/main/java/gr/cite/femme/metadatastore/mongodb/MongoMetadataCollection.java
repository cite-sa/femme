package gr.cite.femme.metadatastore.mongodb;

import java.time.Instant;
import java.util.List;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.model.Status;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;
import org.bson.types.ObjectId;

public interface MongoMetadataCollection {
	
	public void insert(Metadatum metadatum) throws MetadataStoreException;

	public void update(Metadatum metadatum) throws MetadataStoreException;

	public void updateStatus(String id, Status status) throws MetadataStoreException;

	public Metadatum get(Metadatum metadatum) throws MetadataStoreException;

	public MongoCursor<Metadatum> findAll(boolean lazy) throws MetadataStoreException;

	public List<Metadatum> find(String elementId) throws MetadataStoreException;

	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException;

	public MongoCursor<Metadatum> findAllBeforeTimestamp(Instant timestamp) throws MetadataStoreException;

	public List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException;

	public void delete(Metadatum metadatum);
	
	public void deleteAll(String elementId) throws MetadataStoreException;
}
