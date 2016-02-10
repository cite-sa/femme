package gr.cite.exmms.manager.criteria.serializer;

import gr.cite.exmms.manager.core.DataElement;
import gr.cite.exmms.manager.core.Metadatum;
import gr.cite.exmms.manager.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.manager.criteria.Where;
import gr.cite.exmms.manager.criteria.WhereBuilder;

public class WhereSerializer<T> implements Where<T> {

	private WhereBuilder<T> builder = new WhereBuilderSerializer<>();

	private Operation operation;

	private Metadatum metadatum;
	private DataElement dataElement;
	private WhereBuilder<T> subexpressionWhereBuilder;
	private WhereBuilder<DataElement> childWhereBuilder;

	@Override
	public WhereBuilder<T> expression(WhereBuilder<T> expression) {
		this.subexpressionWhereBuilder = expression;
		operation = Operation.EXPRESSION;
		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> expression(S metadatum) {
		this.metadatum = metadatum;
		operation = Operation.EXPRESSION;
		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> exists(S metadatum) {
		this.metadatum = metadatum;
		operation = Operation.EXISTS;
		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum) throws UnsupportedQueryOperationException {
		this.metadatum = metadatum;
		operation = Operation.IS_PARENT_OF;
		return builder;
	}

	@Override
	public <S extends DataElement> WhereBuilder<T> isParentOf(S dataelement) throws UnsupportedQueryOperationException {
		this.dataElement = dataelement;
		operation = Operation.IS_PARENT_OF;
		return builder;
	}

	@Override
	public <S extends DataElement> WhereBuilder<T> isChildOf(S dataElement) {
		this.dataElement = dataElement;
		operation = Operation.IS_CHILD_OF;
		return builder;
	}

	@Override
	public <S extends DataElement> WhereBuilder<T> isChildOf(WhereBuilder<S> where) {
		this.childWhereBuilder = (WhereBuilder<DataElement>) where;
		operation = Operation.IS_CHILD_OF;
		return builder;
	}

	public Metadatum getMetadatum() {
		return metadatum;
	}

	public void setMetadatum(Metadatum metadatum) {
		this.metadatum = metadatum;
	}

	public WhereBuilder<T> getBuilder() {
		return builder;
	}

	public void setBuilder(WhereBuilder<T> builder) {
		this.builder = builder;
	}

	public DataElement getDataelement() {
		return dataElement;
	}

	public void setDataelement(DataElement dataelement) {
		this.dataElement = dataelement;
	}

	public DataElement getDataElement() {
		return dataElement;
	}

	public void setDataElement(DataElement dataElement) {
		this.dataElement = dataElement;
	}

	public WhereBuilder<T> getSubexpressionWhereBuilder() {
		return subexpressionWhereBuilder;
	}

	public void setSubexpressionWhereBuilder(WhereBuilder<T> subexpressionWhereBuilder) {
		this.subexpressionWhereBuilder = subexpressionWhereBuilder;
	}

	public WhereBuilder<DataElement> getChildWhereBuilder() {
		return childWhereBuilder;
	}

	public void setChildWhereBuilder(WhereBuilder<DataElement> childWhereBuilder) {
		this.childWhereBuilder = childWhereBuilder;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

}
