package gr.cite.exmms.manager.criteria;

import gr.cite.exmms.manager.core.DataElement;
import gr.cite.exmms.manager.core.DataElementMetadatum;
import gr.cite.exmms.manager.core.Metadatum;

public interface Where<T> {

	WhereBuilder<T> expression(WhereBuilder<T> expression);

	<S extends Metadatum> WhereBuilder<T> expression(S metadatum);

	<S extends Metadatum> WhereBuilder<T> exists(S metadatum);

	<S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum);

	<S extends DataElement> WhereBuilder<T> isParentOf(S dataElement);

	<S extends DataElement> WhereBuilder<T> isChildOf(S dataElement);

}
