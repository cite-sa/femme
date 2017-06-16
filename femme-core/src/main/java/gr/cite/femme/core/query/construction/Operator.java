package gr.cite.femme.core.query.construction;

import java.util.List;

public interface Operator<T extends Criterion> {

	public Operator<T> or(T... criteria);

	public Operator<T> or(List<T> criteria);

	public Operator<T> and(T... criteria);

	public Operator<T> and(List<T> criteria);

	public Operator<T> not(T... criteria);

	public Operator<T> not(List<T> criteria);

	public Operator<T> nor(T... criteria);

	public Operator<T> nor(List<T> criteria);
	
	public Operator<T> eq(String field, Object value);
	
	public Operator<T> gt(String field, Object value);
	
	public Operator<T> gte(String field, Object value);
	
	public Operator<T> lt(String field, Object value);
	
	public Operator<T> lte(String field, Object value);
	
	public Operator<T> ne(String field, Object value);
	
	public Operator<T> in(String field, Object value);
	
	public Operator<T> nin(String field, Object value);

	public Operator<T> inCollections(T... criteria);

	public Operator<T> inCollections(List<T> criteria);

	public Operator<T> inAnyCollection(T... criteria);

	public Operator<T> inAnyCollection(List<T> criteria);

	public Operator<T> hasDataElements(T... criteria);

	public Operator<T> hasDataElements(List<T> criteria);

	public Operator<T> hasAnyDataElement(T... criteria);
	
	public Operator<T> hasAnyDataElement(List<T> criteria);
}
