package gr.cite.femme.core.datastores;

import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;

public class DatastoreRepositoryProvider {
	private DatastoreRepository<Collection> collectionDatastoreRepository;
	private DatastoreRepository<DataElement> dataElementDatastoreRepository;
	
	public DatastoreRepositoryProvider(DatastoreRepository<Collection> collectionDatastoreRepository, DatastoreRepository<DataElement> dataElementDatastoreRepository) {
		this.collectionDatastoreRepository = collectionDatastoreRepository;
		this.dataElementDatastoreRepository = dataElementDatastoreRepository;
	}
	
	public <T extends Element> DatastoreRepository<T> get(T element) {
		if (Collection.class.equals(element.getClass())) {
			return (DatastoreRepository<T>) this.collectionDatastoreRepository;
		} else if (DataElement.class.equals(element.getClass())) {
			return (DatastoreRepository<T>) this.dataElementDatastoreRepository;
		} else {
			throw new IllegalArgumentException("[" + element.getClass().getSimpleName() + "] Invalid datastore type");
		}
	}
	
	public <T extends Element> DatastoreRepository<T> get(Class<T> elementSubtype) {
		if (Collection.class.equals(elementSubtype)) {
			return (DatastoreRepository<T>) this.collectionDatastoreRepository;
		} else if (DataElement.class.equals(elementSubtype)) {
			return (DatastoreRepository<T>) this.dataElementDatastoreRepository;
		} else {
			throw new IllegalArgumentException("[" + elementSubtype.getSimpleName() + "] Invalid datastore type");
		}
	}
}
