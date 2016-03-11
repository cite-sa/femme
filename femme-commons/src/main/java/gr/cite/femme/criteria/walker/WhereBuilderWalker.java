package gr.cite.femme.criteria.walker;

import gr.cite.femme.criteria.CriteriaQuery;
import gr.cite.femme.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.criteria.Where;
import gr.cite.femme.criteria.WhereBuilder;
import gr.cite.femme.criteria.serializer.WhereBuilderSerializer;

/**
 * 
 * @param <T>
 *            where generic type
 * @param <S>
 *            {@linkplain CriteriaQuery} generic type
 * 
 */
public class WhereBuilderWalker<T, S> {

	public static <T, S> CriteriaQuery<T> walk(WhereBuilderSerializer<T> whereSerializer, WhereBuilder<T> datastoreWhere,
			CriteriaQuery<S> datastoreQuery) throws UnsupportedQueryOperationException {
		WhereBuilderWalker<T, S> walker = new WhereBuilderWalker<>(whereSerializer, datastoreWhere, datastoreQuery);
		return walker.walk();
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

	CriteriaQuery<T> walk() throws UnsupportedQueryOperationException {
		if (whereBuilderSerializer == null || whereBuilderSerializer.getOperation() == null) {
			return datastoreWhereBuilder.build();
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

		return WhereWalker.walk(whereBuilderSerializer.getWhere(), where, datastoreQuery);
	}

	public Where<T> getWhere() {
		return where;
	}
}
