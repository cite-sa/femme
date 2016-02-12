package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;
import gr.cite.exmms.criteria.serializer.WhereBuilderSerializer;

public class WhereBuilderWalker<T> {

	public static <T> Where<T> walk(WhereBuilderSerializer<T> whereSerializer, WhereBuilder<T> datastoreWhere) {
		WhereBuilderWalker<T> walker = new WhereBuilderWalker<>(whereSerializer, datastoreWhere);
		walker.walk();
		return walker.getWhere();
	}

	WhereBuilderSerializer<T> whereBuilderSerializer;

	WhereBuilder<T> datastoreWhereBuilder;

	private Where<T> where;

	public WhereBuilderWalker(WhereBuilderSerializer<T> whereBuilderSerializer, WhereBuilder<T> datastoreWhereBuilder) {
		super();
		this.whereBuilderSerializer = whereBuilderSerializer;
		this.datastoreWhereBuilder = datastoreWhereBuilder;
	}

	void walk() {
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

		WhereWalker.walk(whereBuilderSerializer.getWhere(), where);
	}

	public Where<T> getWhere() {
		return where;
	}
}
