package gr.cite.femme.query.criteria;

import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;

public interface Where<T> {

	WhereBuilder<T> expression(WhereBuilder<T> expression);

	<S extends Metadatum> WhereBuilder<T> expression(S metadatum);

	/*<S extends Metadatum> WhereBuilder<T> exists(S metadatum);*/

	<S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum) throws UnsupportedQueryOperationException;

	<S extends Element> WhereBuilder<T> isParentOf(S dataElement) throws UnsupportedQueryOperationException;

	<S extends Element> WhereBuilder<T> isChildOf(S dataElement);
	
	/**
	 * is child of a S element which validates the {@linkplain WhereBuilder where}
	 * @param where
	 * @return
	 */
	<S extends Element> WhereBuilder<T> isChildOf(WhereBuilder<S> where);

}
