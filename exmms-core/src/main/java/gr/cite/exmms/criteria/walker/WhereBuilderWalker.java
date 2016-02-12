package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;
import gr.cite.exmms.criteria.serializer.WhereBuilderSerializer;

/**
 * 
 * @param <T>
 *            where generic type
 * @param <S>
 *            {@linkplain CriteriaQuery} generic type
 * 
 */
public class WhereBuilderWalker<T, S> {

	public static <T, S> Where<T> walk(WhereBuilderSerializer<T> whereSerializer, WhereBuilder<T> datastoreWhere,
			CriteriaQuery<S> datastoreQuery) throws UnsupportedQueryOperationException {
		WhereBuilderWalker<T, S> walker = new WhereBuilderWalker<>(whereSerializer, datastoreWhere, datastoreQuery);
		walker.walk();
		return walker.getWhere();
	}

	WhereBuilderSerializer<T> whereBuilderSerializer;

	WhereBuilder<T> datastoreWhereBuilder;

	private Where<T> where;

	private CriteriaQuery<S> datastoreQuery;

	public WhereBuilderWalker(WhereBuilderSerializer<T> whereBuilderSerializer, WhereBuilder<T> datastoreWhereBuilder,
			CriteriaQuery<S> datastoreQuery) {
		super();
		this.whereBuilderSerializer = whereBuilderSerializer;
		this.datastoreWhereBuilder = datastoreWhereBuilder;
		this.datastoreQuery = datastoreQuery;

	}

	void walk() throws UnsupportedQueryOperationException {
		if (whereBuilderSerializer == null || whereBuilderSerializer.getOperation() == null) {
			return;
		}

		switch (whereBuilderSerializer.getOperation()) {
		case OR:

			where = datastoreWhereBuilder.or();

			break;

		case AND:

			where = datastoreWhereBuilder.and();

			break;
		default:
			break;
		}

		WhereWalker.walk(whereBuilderSerializer.getWhere(), where, datastoreQuery);
	}

	public Where<T> getWhere() {
		return where;
	}
}
