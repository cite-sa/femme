package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;
import gr.cite.exmms.criteria.serializer.WhereSerializer;

public class WhereWalker<T> {

	public static <T> WhereBuilder<T> walk(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere) {
		WhereWalker<T> walker = new WhereWalker<>(whereSerializer, datastoreWhere);
		walker.walk();
		return walker.getWhereBuilder();
	}

	WhereSerializer<T> whereSerializer;

	Where<T> datastoreWhere;

	WhereBuilder<T> whereBuilder;

	public WhereWalker(WhereSerializer<T> whereSerializer, Where<T> datastoreWhere) {
		super();
		this.whereSerializer = whereSerializer;
		this.datastoreWhere = datastoreWhere;
	}

	void walk() {
		switch (whereSerializer.getOperation()) {
		case EXPRESSION:
			if (whereSerializer.getMetadatum() != null) {
				whereBuilder = datastoreWhere.expression(whereSerializer.getMetadatum());
			} else {
				whereBuilder = datastoreWhere.expression(null); // TODO
																// whereBuilder
																// walk
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
				whereBuilder = datastoreWhere.isChildOf(null); // TODO
																// whereBuilder
																// walk
			}

			break;
		default:
			break;
		}
		
		WhereBuilderWalker.walk(whereSerializer.getBuilder(), whereBuilder);

	}

	public WhereBuilder<T> getWhereBuilder() {
		return whereBuilder;
	}
}
