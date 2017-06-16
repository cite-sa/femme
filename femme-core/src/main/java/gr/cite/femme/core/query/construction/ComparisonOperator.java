package gr.cite.femme.core.query.construction;

public interface ComparisonOperator {
	
	public void eq(String field, Object value);
	
	public void gt(String field, Object value);
	
	public void gte(String field, Object value);
	
	public void lt(String field, Object value);
	
	public void lte(String field, Object value);
	
	public void ne(String field, Object value);
	
	public void in(String field, Object value);
	
	public void nin(String field, Object value);
	
}
