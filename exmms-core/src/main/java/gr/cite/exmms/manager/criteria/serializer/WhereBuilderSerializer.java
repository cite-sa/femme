package gr.cite.exmms.manager.criteria.serializer;

import gr.cite.exmms.manager.criteria.CriteriaQuery;
import gr.cite.exmms.manager.criteria.Where;
import gr.cite.exmms.manager.criteria.WhereBuilder;

public class WhereBuilderSerializer<T> implements WhereBuilder<T> {

	private Where<T> where = new WhereSerializer<>();

	private Operation operation;
	
	@Override
	public Where<T> or() {
		this.operation = Operation.OR;
		return where;
	}

	@Override
	public Where<T> and() {
		this.operation = Operation.AND;
		return where;
	}

	@Override
	public CriteriaQuery<T> build() {
		return null;
	}

}
