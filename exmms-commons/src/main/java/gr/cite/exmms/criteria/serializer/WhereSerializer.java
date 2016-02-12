package gr.cite.exmms.criteria.serializer;

import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;

public class WhereSerializer<T> implements Where<T> {

	private WhereBuilderSerializer<T> builder;

	private Operation operation;

	private Metadatum metadatum;
	private Element dataElement;
	private WhereBuilderSerializer<T> subexpressionWhereBuilder;
	private WhereBuilderSerializer<Element> childWhereBuilder;

	boolean keepStack = false;

	WhereSerializer<T> parent;

	public WhereSerializer(boolean keepStack) {
		this.keepStack = keepStack;
		parent = this;

	}

	public WhereSerializer() {

	}

	public WhereSerializer(WhereSerializer<T> parent) {
		this.parent = parent;

	}

	@Override
	public WhereBuilder<T> expression(WhereBuilder<T> expression) {
		this.subexpressionWhereBuilder = (WhereBuilderSerializer<T>) expression;
		subexpressionWhereBuilder.build();
		operation = Operation.EXPRESSION;
		builder = new WhereBuilderSerializer<>(parent);
		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> expression(S metadatum) {
		this.metadatum = metadatum;
		operation = Operation.EXPRESSION;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> exists(S metadatum) {
		this.metadatum = metadatum;
		operation = Operation.EXISTS;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	@Override
	public <S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum) throws UnsupportedQueryOperationException {
		this.metadatum = metadatum;
		operation = Operation.IS_PARENT_OF;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	@Override
	public <S extends Element> WhereBuilder<T> isParentOf(S dataelement) throws UnsupportedQueryOperationException {
		this.dataElement = dataelement;
		operation = Operation.IS_PARENT_OF;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	@Override
	public <S extends Element> WhereBuilder<T> isChildOf(S dataElement) {
		this.dataElement = dataElement;
		operation = Operation.IS_CHILD_OF;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	@Override
	public <S extends Element> WhereBuilder<T> isChildOf(WhereBuilder<S> expression) {
		this.childWhereBuilder = (WhereBuilderSerializer<Element>) expression;
		childWhereBuilder.build();
		operation = Operation.IS_CHILD_OF;
		builder = new WhereBuilderSerializer<>(parent);

		return builder;
	}

	public Metadatum getMetadatum() {
		return metadatum;
	}

	public void setMetadatum(Metadatum metadatum) {
		this.metadatum = metadatum;
	}

	public WhereBuilderSerializer<T> getBuilder() {
		return builder;
	}

	public void setBuilder(WhereBuilderSerializer<T> builder) {
		this.builder = builder;
	}

	public Element getDataElement() {
		return dataElement;
	}

	public void setDataElement(Element dataElement) {
		this.dataElement = dataElement;
	}

	public WhereBuilderSerializer<T> getSubexpressionWhereBuilder() {
		return subexpressionWhereBuilder;
	}

	public void setSubexpressionWhereBuilder(WhereBuilderSerializer<T> subexpressionWhereBuilder) {
		this.subexpressionWhereBuilder = subexpressionWhereBuilder;
	}

	public WhereBuilderSerializer<Element> getChildWhereBuilder() {
		return childWhereBuilder;
	}

	public void setChildWhereBuilder(WhereBuilderSerializer<Element> childWhereBuilder) {
		this.childWhereBuilder = childWhereBuilder;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public void setParent(WhereSerializer<T> parent) {
		this.parent = parent;
	}

}
