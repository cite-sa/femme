package gr.cite.femme.criteria.walker;

import gr.cite.femme.core.Element;
import gr.cite.femme.criteria.CriteriaQuery;
import gr.cite.femme.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.criteria.Where;
import gr.cite.femme.criteria.WhereBuilder;
import gr.cite.femme.criteria.serializer.WhereSerializer;

/**
 * 
 * @param <T>
 *            where generic type
 * @param <S>
 *            {@linkplain CriteriaQuery} generic type
 */
public class WhereWalker<T, S> {

	public static <T, S> WhereBuilder<T> walkSubexpression(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere,
			CriteriaQuery<S> datastoreQuery) throws UnsupportedQueryOperationException {
		WhereWalker<T, S> walker = new WhereWalker<>(whereSerializer, datastoreWhere, datastoreQuery);
		walker.walk();
		return walker.getWhereBuilder();
	}

	public static <T, S> CriteriaQuery<T> walk(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere,
			CriteriaQuery<S> datastoreQuery) throws UnsupportedQueryOperationException {
		WhereWalker<T, S> walker = new WhereWalker<>(whereSerializer, datastoreWhere, datastoreQuery);
		return walker.walk();
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

	CriteriaQuery<T> walk() throws UnsupportedQueryOperationException {
		if (whereSerializer == null || whereSerializer.getOperation() == null) {
			return null;
		}

		switch (whereSerializer.getOperation()) {
		case EXPRESSION:
			if (whereSerializer.getMetadatum() != null) {
				whereBuilder = datastoreWhere.expression(whereSerializer.getMetadatum());
			} else {

				whereBuilder = datastoreWhere
						.expression(WhereWalker.walkSubexpression(whereSerializer.getSubexpressionWhereBuilder().getParent(),
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
				WhereBuilder<Element> builder = WhereWalker.walkSubexpression(whereSerializer.getChildWhereBuilder().getParent(),
						datastoreQuery.expressionFactory(), datastoreQuery);

				whereBuilder = datastoreWhere.isChildOf(builder);
			}

			break;
		default:
			break;
		}

		return WhereBuilderWalker.walk(whereSerializer.getBuilder(), whereBuilder, datastoreQuery);

	}

	public WhereBuilder<T> getWhereBuilder() {
		return whereBuilder;
	}
}
