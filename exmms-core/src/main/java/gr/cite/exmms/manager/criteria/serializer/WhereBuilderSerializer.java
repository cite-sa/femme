package gr.cite.exmms.manager.criteria.serializer;

import gr.cite.exmms.manager.criteria.CriteriaQuery;
import gr.cite.exmms.manager.criteria.Where;
import gr.cite.exmms.manager.criteria.WhereBuilder;

public class WhereBuilderSerializer<T> implements WhereBuilder<T> {

	private Where<T> where;

	private Operation operation;

	private WhereSerializer<T> parent = null;

	public WhereBuilderSerializer(WhereSerializer<T> parent) {
		this.parent = parent;
		System.out.println(this + " ->" + parent);
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
			
			WhereSerializer<T> topWhere = null;
			while (topWhereBuilder != this) {
				topWhereBuilder.setParent(null);
				topWhere = (WhereSerializer<T>) topWhereBuilder.getWhere();
				topWhere.setParent(null);
				topWhereBuilder = (WhereBuilderSerializer<T>) ((WhereSerializer<T>) (topWhereBuilder.getWhere())).getBuilder();		
			}
			if (topWhere != null) {
				topWhere.setBuilder(null);
			}
		}
		return null;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Where<T> getWhere() {
		return where;
	}

	public void setWhere(Where<T> where) {
		this.where = where;
	}

	public WhereSerializer<T> getParent() {
		return parent;
	}

	public void setParent(WhereSerializer<T> parent) {
		this.parent = parent;
	}

}
