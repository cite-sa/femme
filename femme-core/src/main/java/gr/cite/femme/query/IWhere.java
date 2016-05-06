package gr.cite.femme.query;

import java.util.List;

public interface IWhere {
	public ICriteria eq(Object s);
	
	public ICriteria gt(Object s);
	
	public ICriteria gte(Object s);
	
	public ICriteria lt(Object s);
	
	public ICriteria lte(Object s);
	
	public ICriteria ne(Object s);
	
	public ICriteria in(List<Object> array);
	
	public ICriteria nin(List<Object> array);

	public ICriteria exists(boolean exists);
}
