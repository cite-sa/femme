package gr.cite.femme.query.api;

import java.util.List;

public interface Operator<T extends Criterion> {

	public Operator<T> or(List<T> criteria);
	
	public Operator<T> and(List<T> criteria);
	
	public Operator<T> not(List<T> criteria);
	
	public Operator<T> nor(List<T> criteria);
	
	public Operator<T> eq(String field, Object value);
	
	public Operator<T> gt(String field, Object value);
	
	public Operator<T> gte(String field, Object value);
	
	public Operator<T> lt(String field, Object value);
	
	public Operator<T> lte(String field, Object value);
	
	public Operator<T> ne(String field, Object value);
	
	public Operator<T> in(String field, Object value);
	
	public Operator<T> nin(String field, Object value);
}
