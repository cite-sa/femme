package gr.cite.exmms.criteria.utils;

import java.util.ArrayList;
import java.util.List;

import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;

public class PrintCriteriaQuery<T extends DataElement> implements CriteriaQuery<T> {

	StringBuilder query = new StringBuilder();

	@Override
	public Where<T> whereBuilder() {
		return new WherePrint<>(this, query);
	}

	@Override
	public Where<T> expressionFactory() {
		return new WherePrint<>(new PrintCriteriaQuery<>());
	}

	@Override
	public T find(String id) {
		System.out.println("query: " + query);
		return null;
	}

	@Override
	public T find(DataElement t) {
		System.out.println("query: " + query);
		return null;
	}

	@Override
	public List<T> find() {
		System.out.println("query: " + query);
		return new ArrayList<>();
	}

	@Override
	public String toString() {
		return query.toString().trim();
	}
	
	public static class WherePrint<T extends DataElement> implements Where<T> {

		StringBuilder query;
		private WhereBuilder<T> whereBuilder;

		public WherePrint(PrintCriteriaQuery<T> printCriteriaQuery, StringBuilder query) {
			this.query = query;
			this.whereBuilder = new WhereBuilderPrint<>(printCriteriaQuery, this, query);
		}
		
		public WherePrint(PrintCriteriaQuery<T> printCriteriaQuery) {
			this.query = new StringBuilder();
			this.whereBuilder = new WhereBuilderPrint<>(printCriteriaQuery, this, query);
		}

		@Override
		public WhereBuilder<T> expression(WhereBuilder<T> expression) {
			query.append(" (").append(((WhereBuilderPrint<T>) expression).getQuery()).append(")");
			return whereBuilder;
		}

		@Override
		public <S extends Metadatum> WhereBuilder<T> expression(S metadatum) {
			query.append(" " + metadatum.getName() + " = " + metadatum.getValue());
			return whereBuilder;
		}

		@Override
		public <S extends Metadatum> WhereBuilder<T> exists(S metadatum) {
			query.append(" exists " + metadatum.getName());
			return whereBuilder;
		}

		@Override
		public <S extends Metadatum> WhereBuilder<T> isParentOf(S metadatum)
				throws UnsupportedQueryOperationException {
			query.append(" isParentOf " + metadatum.getName());
			return whereBuilder;
		}

		@Override
		public <S extends Element> WhereBuilder<T> isParentOf(S dataElement)
				throws UnsupportedQueryOperationException {
			query.append(" isParentOf " + dataElement.getId());
			return whereBuilder;
		}

		@Override
		public <S extends Element> WhereBuilder<T> isChildOf(S dataElement) {
			query.append(" isChildOf " + dataElement.getId());
			return whereBuilder;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S extends Element> WhereBuilder<T> isChildOf(WhereBuilder<S> where) {
			query.append(" isChildOf(").append(((WhereBuilderPrint<T>) where).getQuery()).append(")");
			return whereBuilder;
		}
		
		@Override
		public String toString() {
			return query.toString();
		}

	}

	public static class WhereBuilderPrint<T extends DataElement> implements WhereBuilder<T> {

		StringBuilder query;
		WherePrint<T> where;
		private CriteriaQuery<T> printCriteriaQuery;

		public WhereBuilderPrint(PrintCriteriaQuery<T> printCriteriaQuery, WherePrint<T> where, StringBuilder query) {
			super();
			this.query = query;
			this.where = where;
			this.printCriteriaQuery = printCriteriaQuery;
		}

		public WhereBuilderPrint() {
			this.query = new StringBuilder();
		}

		public StringBuilder getQuery() {
			return query;
		}

		@Override
		public Where<T> or() {
			query.append(" or");
			return where;
		}

		@Override
		public Where<T> and() {
			query.append(" and");
			return where;
		}

		@Override
		public CriteriaQuery<T> build() {
			return printCriteriaQuery;
		}
		
		@Override
		public String toString() {
			return query.toString();
		}

	}

}
