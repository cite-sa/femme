package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.core.Element;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;
import gr.cite.exmms.criteria.serializer.WhereSerializer;

/**
 * 
 * @param <T>
 *            where generic type
 * @param <S>
 *            {@linkplain CriteriaQuery} generic type
 */
public class WhereWalker<T, S> {

	public static <T, S> WhereBuilder<T> walk(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere,
			CriteriaQuery<S> datastoreQuery) throws UnsupportedQueryOperationException {
		WhereWalker<T, S> walker = new WhereWalker<>(whereSerializer, datastoreWhere, datastoreQuery);
		walker.walk();
		return walker.getWhereBuilder();
	}

	WhereSerializer<T> whereSerializer;

	Where<T> datastoreWhere;

	WhereBuilder<T> whereBuilder;

	private CriteriaQuery<S> datastoreQuery;

	public WhereWalker(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere, CriteriaQuery<S> datastoreQuery) {
		super();
		this.whereSerializer = whereSerializer;
		this.datastoreWhere = datastoreWhere;
		this.datastoreQuery = datastoreQuery;
	}

	void walk() throws UnsupportedQueryOperationException {
		if (whereSerializer == null || whereSerializer.getOperation() == null) {
			return;
		}

		switch (whereSerializer.getOperation()) {
		case EXPRESSION:
			if (whereSerializer.getMetadatum() != null) {
				whereBuilder = datastoreWhere.expression(whereSerializer.getMetadatum());
			} else {

				whereBuilder = datastoreWhere
						.expression(WhereWalker.walk(whereSerializer.getSubexpressionWhereBuilder().getParent(),
								datastoreQuery.expressionFactory(), datastoreQuery));
			}

			break;

		case EXISTS:
			whereBuilder = datastoreWhere.exists(whereSerializer.getMetadatum());

			break;

		case IS_PARENT_OF:
			if (whereSerializer.getMetadatum() != null) {
				whereBuilder = datastoreWhere.isParentOf(whereSerializer.getMetadatum());
			} else if (whereSerializer.getDataElement() != null) {
				whereBuilder = datastoreWhere.isParentOf(whereSerializer.getDataElement());
			}

			break;

		case IS_CHILD_OF:
			if (whereSerializer.getDataElement() != null) {
				whereBuilder = datastoreWhere.isChildOf(whereSerializer.getDataElement());
			} else {
				WhereBuilder<Element> builder = WhereWalker.walk(whereSerializer.getChildWhereBuilder().getParent(),
						datastoreQuery.expressionFactory(), datastoreQuery);

				whereBuilder = datastoreWhere.isChildOf(builder);
			}

			break;
		default:
			break;
		}

		WhereBuilderWalker.walk(whereSerializer.getBuilder(), whereBuilder, datastoreQuery);

	}

	public WhereBuilder<T> getWhereBuilder() {
		return whereBuilder;
	}
}
