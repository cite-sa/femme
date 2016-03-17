package gr.cite.femme.criteria.mongodb;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.criteria.Where;
import gr.cite.femme.criteria.WhereBuilder;

public class MongoWhere<T extends Element> implements Where<T> {

	@Override
	public WhereBuilder<T> expression(WhereBuilder<T> expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> expression(S metadatum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> exists(S metadatum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum) throws UnsupportedQueryOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends gr.cite.femme.core.Element> WhereBuilder<T> isParentOf(S metadatum)
			throws UnsupportedQueryOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends gr.cite.femme.core.Element> WhereBuilder<T> isChildOf(S dataElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends gr.cite.femme.core.Element> WhereBuilder<T> isChildOf(WhereBuilder<S> where) {
		// TODO Auto-generated method stub
		return null;
	}

}
