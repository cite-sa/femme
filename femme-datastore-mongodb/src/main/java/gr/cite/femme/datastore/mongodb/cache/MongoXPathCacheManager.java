/*package gr.cite.femme.datastore.mongodb.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.core.MetadatumXPathCache;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.query.ICriteria;

public class MongoXPathCacheManager implements XPathCacheManager {

	MongoDatastore datastore;

	public MongoXPathCacheManager(MongoDatastore datastore) {
		this.datastore = datastore;
	}

	@Override
	public void createIndex() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeIndex() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Element> void checkAndCreateIndexOnXPath(Metadatum metadatum, String xpath, List<String> xPathResult, T element) {
		
		List<MetadatumXPathCache> metadatumIndexes =  metadatum.getIndex().stream().filter(new Predicate<MetadatumXPathCache>() {

			@Override
			public boolean test(MetadatumXPathCache index) {
				if (index.getXPath().equals(xpath)) {
					return true;
				}
				return false;
			}
		}).collect(Collectors.toList());
		
		boolean indexExists = false;
		boolean indexUnchanged = false;
		int updatedIndexPosition = 0;
		MetadatumXPathCache updatedIndex = null;
		
		if (metadatum.getXPathCache() == null) {
			metadatum.setXPathCache(new ArrayList<>());
		}
			for (int i = 0; i < metadatum.getXPathCache().size(); i ++) {
				updatedIndex = metadatum.getXPathCache().get(i);
				if (updatedIndex.getXPath().equals(xpath)) {
					indexExists = true;
					if (updatedIndex.getValues().retainAll(xPathResult)) {
						updatedIndex.setValues(xPathResult);
						updatedIndexPosition = i;
					} else {
						indexUnchanged = true;
					}
					
				}
			}
			for (MetadatumXPathCache metadatumIndex: metadatum.getIndex()) {
				if (metadatumIndex.getXPath().equals(xpath)) {
					metadatumIndex.setValues(xPathResult);
					newIndex = metadatumIndex;
					indexExists = true;
				}
			}
			
			if (!indexExists) {
				updatedIndex = new MetadatumXPathCache();
				updatedIndex.setXPath(xpath);
				updatedIndex.setValues(xPathResult);
				metadatum.getXPathCache().add(updatedIndex);
			}
			
			if (indexExists) {
				if (!indexUnchanged) {
					getMongoCollection(element).updateOne(
						Filters.eq(FieldNames.ID, new ObjectId(metadatum.getElementId())),
						Filters.and(
								Filters.eq(FieldNames.ID, new ObjectId(metadatum.getElementId())),
								Filters.eq(FieldNames.METADATA + "." + "index." + updatedIndexPosition + "", value)),
						Updates.set(
							"metadata." + getIndexOfMetadatum(metadatum, element) + ".index." + updatedIndexPosition,
							updatedIndex
							new Document().append("_id", new ObjectId()).append("xPath", xpath).append("values", xPathResult)
						)
					);
				}
			} else {
				getMongoCollection(element).updateOne(
						Filters.eq(FieldNames.ID, new ObjectId(metadatum.getElementId())),
						Updates.addToSet(
							"metadata." + getIndexOfMetadatum(metadatum, element) + ".index",
							updatedIndex
							new Document().append("_id", new ObjectId()).append("xPath", xpath).append("values", xPathResult)
						)
					);
			}
	}

	@Override
	public void removeIndexOnXPath() {
		// TODO Auto-generated method stub

	}

	private <T extends Element> MongoCollection<T> getMongoCollection(T element) {
		MongoCollection<T> mongoCollection = null;
		if (element instanceof Collection) {
			mongoCollection = (MongoCollection<T>) datastore.getCollections();
		} else if (element instanceof DataElement) {
			mongoCollection = (MongoCollection<T>) datastore.getDataElements();
		}
		return mongoCollection;
	}

	private <T extends Element> int getIndexOfMetadatum(Metadatum metadatum, T element) {
		for (int i = 0; i < element.getMetadata().size(); i++) {
			if (element.getMetadata().get(i).getId().equals(metadatum.getId())) {
				return i;
			}
		}
		return -1;
	}
}
*/