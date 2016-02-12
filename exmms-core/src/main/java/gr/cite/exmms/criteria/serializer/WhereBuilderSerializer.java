package gr.cite.exmms.criteria.serializer;

import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;

public class WhereBuilderSerializer<T> implements WhereBuilder<T> {

	private WhereSerializer<T> where;

	private Operation operation;

	private WhereSerializer<T> parent = null;

	public WhereBuilderSerializer(WhereSerializer<T> parent) {
		this.parent = parent;
	}

	public WhereBuilderSerializer() {
	}

	@Override
	public Where<T> or() {
		this.operation = Operation.OR;

		if (parent == null) {
			where = new WhereSerializer<>();
		} else {
			where = new WhereSerializer<>(parent);
		}

		return where;
	}

	@Override
	public Where<T> and() {
		this.operation = Operation.AND;

		if (parent == null) {
			where = new WhereSerializer<>();
		} else {
			where = new WhereSerializer<>(parent);
		}

		return where;
	}

	@Override
	public CriteriaQuery<T> build() {
		if (parent != null) {
			// stop recursion
			WhereBuilderSerializer<T> topWhereBuilder = (WhereBuilderSerializer<T>) this.parent.getBuilder();
			this.parent.setParent(null);

			WhereSerializer<T> topWhere = this.parent;

			while (topWhereBuilder != this) {
				topWhereBuilder.setParent(null);
				topWhere = (WhereSerializer<T>) topWhereBuilder.getWhere();
				topWhere.setParent(null);
				topWhereBuilder = (WhereBuilderSerializer<T>) topWhere.getBuilder();
			}
			topWhere.setBuilder(null);
		}
		return null;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public WhereSerializer<T> getWhere() {
		return where;
	}

	public void setWhere(WhereSerializer<T> where) {
		this.where = where;
	}

	public WhereSerializer<T> getParent() {
		return parent;
	}

	public void setParent(WhereSerializer<T> parent) {
		this.parent = parent;
	}

}
