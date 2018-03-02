package gr.cite.femme.engine.query.execution.mongodb;

import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.execution.MetadataQueryExecutor;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataQueryMongoExecutor<T extends Element> extends QueryMongoExecutor<T> implements MetadataQueryExecutor<T> {
	private static final Logger logger = LoggerFactory.getLogger(MetadataQueryMongoExecutor.class);

	private MetadataStore metadataStore;
	private List<Metadatum> metadataXPathResults = null;

	public MetadataQueryMongoExecutor(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		super(datastore, elementSubtype);
		this.metadataStore = metadataStore;
	}

	public MetadataQueryExecutor<T> options(QueryOptionsMessenger options) {
		super.options(options);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> find(Query<? extends Criterion> query) {
		super.find(query);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPath(String xPath) throws DatastoreException, MetadataStoreException {
		xPath(new ArrayList<>(), xPath);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPath(List<String> elementIds, String xPath) throws MetadataStoreException {
		if (xPath != null && !xPath.trim().isEmpty()) {
			//List<Metadatum> metadataXPathResults;

			long xPathQueryStart = System.currentTimeMillis();

			this.metadataXPathResults = this.metadataStore.xPath(elementIds, xPath, isLazyMetadata());

			long xPathQueryEnd = System.currentTimeMillis();
			logger.info("[" + xPath + "] XPath metadata query time: " + (xPathQueryEnd - xPathQueryStart) + " ms");

			Document xPathSatisfyingElementsQuery = new Document()
					.append(FieldNames.ID,
							new Document().append("$in",
									this.metadataXPathResults.stream()/*.filter(metadatum -> metadatum.getId() != null)*/
											.map(metadatum -> new ObjectId(metadatum.getElementId()))
											.distinct().collect(Collectors.toList())));
			//logger.debug(xPathSatisfyingElementsQuery.toString());

			super.setQueryDocument(getQueryDocument() == null ?
					xPathSatisfyingElementsQuery : new Document().append("$and", Arrays.asList(getQueryDocument(), xPathSatisfyingElementsQuery)));
		}
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPathInMemory(String xPath) throws MetadataStoreException {
		if (xPath != null && ! xPath.trim().isEmpty()) {

			long xPathQueryStart = System.currentTimeMillis();

			this.metadataXPathResults = this.metadataStore.xPathInMemory(xPath);

			long xPathQueryEnd = System.currentTimeMillis();

			logger.info("[" + xPath + "] XPath in memory metadata query time: " + (xPathQueryEnd - xPathQueryStart) + " ms");

			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID, new Document().append("$in",
							this.metadataXPathResults.stream().map(metadatum -> new ObjectId(metadatum.getElementId())).distinct().collect(Collectors.toList())));
									//this.metadataXPathResults.keySet().stream().map(ObjectId::new).collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			super.setQueryDocument(super.getQueryDocument() == null ?
					retrieveXPathSatisfyElementsQuery : new Document().append("$and", Arrays.asList(super.getQueryDocument(), retrieveXPathSatisfyElementsQuery)));
		}

		return this;
	}
	
	public MetadataQueryExecutor<T> xPathInMemory(List<String> elementIds, String xPath) throws DatastoreException, MetadataStoreException {
		if (xPath != null && ! xPath.trim().isEmpty()) {
			
			long xPathQueryStart = System.currentTimeMillis();
			
			this.metadataXPathResults = this.metadataStore.xPathInMemory(elementIds, xPath);
			
			long xPathQueryEnd = System.currentTimeMillis();
			
			logger.info("[" + xPath + "] XPath in memory metadata query time: " + (xPathQueryEnd - xPathQueryStart) + " ms");
			
			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID, new Document().append("$in",
							this.metadataXPathResults.stream().map(metadatum -> new ObjectId(metadatum.getElementId())).distinct().collect(Collectors.toList())));
			//this.metadataXPathResults.keySet().stream().map(ObjectId::new).collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());
			
			super.setQueryDocument(super.getQueryDocument() == null ?
					retrieveXPathSatisfyElementsQuery : new Document().append("$and", Arrays.asList(super.getQueryDocument(), retrieveXPathSatisfyElementsQuery)));
		}
		
		return this;
	}

	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		List<T> elements = super.list();
		if (!isLazyMetadata()) {
			try {
				setReturnedMetadata(elements);
			} catch (RuntimeException e) {
				throw new MetadataStoreException(e.getMessage(), e);
			}
		}

		return elements;
	}
	
	private void setReturnedMetadata(List<T> elements) {
		elements.forEach(element -> {
			if (this.metadataXPathResults != null) {
				List<Metadatum> xPathResult = filterElementMetadataByXPathResults(element);
				if (this.metadataXPathResults.size() > 0) {
					element.setMetadata(xPathResult);
				}
			} else {
				try {
					element.setMetadata(this.metadataStore.find(element.getId(), isLazyMetadata(), Boolean.valueOf(getOptions().isLoadInactiveMetadata())));
				} catch (MetadataStoreException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});
	}
	
	private List<Metadatum> filterElementMetadataByXPathResults(T element) {
		return this.metadataXPathResults.stream()
				.filter(metadatum -> metadatum.getElementId().equals(element.getId()))
				.map(metadatum -> {
					if (metadatum.getValue() == null) {
						try {
							return this.metadataStore.get(metadatum, false);
						} catch (MetadataStoreException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					} else {
						Metadatum originalMetadatum;
						try {
							originalMetadatum = this.metadataStore.get(metadatum, true);
						} catch (MetadataStoreException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
						
						originalMetadatum.setValue(metadatum.getValue());
						return originalMetadatum;
					}
				}).collect(Collectors.toList());
	}

	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		T element = super.first();

		if (element != null) {
			if (!isLazyMetadata()) {
				if (this.metadataXPathResults != null) {
					List<Metadatum> xPathResult = this.metadataXPathResults.stream()
							.filter(metadatum -> metadatum.getElementId().equals(element.getId()))
							.map(metadatum -> {
								if (metadatum.getValue() == null) {
									try {
										return this.metadataStore.get(metadatum, false);
									} catch (MetadataStoreException e) {
										throw new RuntimeException(e.getMessage(), e);
									}
								} else {
								/*//Metadatum metadatumInfo = this.metadataStore.get(metadatum, true);
								Metadatum metadatumResponse = new Metadatum();
								metadatumResponse.setId(metadatum.getId());
								metadatumResponse.setValue(metadatum.getValue());
								return metadatumResponse;*/

									Metadatum originalMetadatum;
									try {
										originalMetadatum = this.metadataStore.get(metadatum, true);
									} catch (MetadataStoreException e) {
										throw new RuntimeException(e.getMessage(), e);
									}

									originalMetadatum.setValue(metadatum.getValue());
									return originalMetadatum;
								}
							}).collect(Collectors.toList());

					if (this.metadataXPathResults.size() > 0) {
						element.setMetadata(xPathResult);
					}
				} else {
					element.setMetadata(this.metadataStore.find(element.getId(), isLazyMetadata(), Boolean.valueOf(getOptions().isLoadInactiveMetadata())));
				}
			}
		}

		//element.setMetadata(this.metadataStore.find(element.getId(), super.isLazyMetadata()));
		return element;
	}

}
