package gr.cite.femme.criteria.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.criteria.CriteriaQuery;
import gr.cite.femme.criteria.Where;
import gr.cite.femme.datastore.mongodb.MongoDatastoreClient;
import gr.cite.femme.datastore.mongodb.bson.CollectionBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.DataElementBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.ElementBson;
import gr.cite.femme.datastore.mongodb.bson.ElementBsonBuilder;

public class MongoCriteriaQuery<T extends Element> implements CriteriaQuery<T> {
	private MongoCollection<Element> mongoCollection;
	
	public MongoCriteriaQuery(MongoDatastoreClient mongoClient) {
		mongoCollection = mongoClient.getElementCollection();
	}

	@Override
	public Where<T> whereBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S> Where<S> expressionFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T find(String id) {
		return (T) mongoCollection.find(new ElementBsonBuilder().id(id).build()).first();
	}

	@Override
	public List<T> find(T element) {
		ElementBson elementToFind = null;
		List<T> elements = new ArrayList<>();
		if (element instanceof DataElement) {
			elementToFind = new DataElementBsonBuilder()
					.id(element.getId())
					.endpoint(element.getEndpoint())
					.name(element.getName())
					.systemicMetadata(element.getSystemicMetadata())
					.dataElement(((DataElement) element).getDataElement())
					.collections(((DataElement) element).getCollections())
					.build();
		} else if (element instanceof Collection) {
			elementToFind = new CollectionBsonBuilder()
				.id(element.getId())
				.endpoint(element.getEndpoint())
				.name(element.getName())
				.systemicMetadata(element.getSystemicMetadata())
				.dataElements(((Collection) element).getDataElements())
				.build();
		}
		MongoCursor<Element> cursor = mongoCollection.find(elementToFind).iterator();
		try {
			while (cursor.hasNext()) {
				elements.add((T)cursor.next());
			}
		} finally {
			cursor.close();
		}
		return elements;
	}

	@Override
	public List<T> find() {
		List<T> elements = new ArrayList<>();
		MongoCursor<Element> cursor = mongoCollection.find().iterator();
		try {
			while (cursor.hasNext()) {
				elements.add((T)cursor.next());
			}
		} finally {
			cursor.close();
		}
		return elements;
	}

}
