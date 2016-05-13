package gr.cite.femme.datastore.mongodb.utils;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.criteria.serializer.WhereBuilderSerializer;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.query.criteria.Where;
import gr.cite.femme.query.criteria.WhereBuilder;

public class MongoWhere implements Where<Element> {
	private MetadataGridFS metadatumGridFS;
	
	private RootQueryDocumentBuilder rootQuery;
	private DataElementQueryDocumentBuilder elementquery;
	private MetadataQueryDocumentBuilder metadataQuery;
	
	private MongoWhereBuilder whereBuilder;
	
	
	public MongoWhere() {
		rootQuery = new RootQueryDocumentBuilder();
		whereBuilder = new MongoWhereBuilder();
	}
	
	public MongoWhere(MetadataGridFS metadatumGridFS) {
		this.metadatumGridFS = metadatumGridFS;
		
		/*elementquery = new DataElementQueryDocumentBuilder(element);*/
	}

	@Override
	public MongoWhereBuilder expression(WhereBuilder<Element> expression) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public <S extends Element> WhereBuilder<Element> expression(S element) throws UnsupportedQueryOperationException {
		rootQuery.element(new DataElementQueryDocumentBuilder(element));
		return whereBuilder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<Element> expression(S metadatum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<Element> isParentOf(S metadatum) throws UnsupportedQueryOperationException {
		/*List<Metadatum> metadata = new ArrayList<>();
		query.setMetadata(metadata);*/
		return null;
	}

	@Override
	public <S extends Element> WhereBuilder<Element> isParentOf(S dataElement) throws UnsupportedQueryOperationException {
		/*if (dataElement instanceof DataElement) {
			query.setDataElement((DataElement) dataElement);
		}*/
		return null;
	}

	@Override
	public <S extends Element> WhereBuilder<Element> isChildOf(S dataElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Element> WhereBuilder<Element> isChildOf(WhereBuilder<S> where) {
		// TODO Auto-generated method stub
		return null;
	}

}
