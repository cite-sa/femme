package gr.cite.femme.query;

import java.util.List;

public interface IWhere<T extends ICriteria> {
	public T eq(Object s);
	
	public T gt(Object s);
	
	public T gte(Object s);
	
	public T lt(Object s);
	
	public T lte(Object s);
	
	public T ne(Object s);
	
	public T in(List<Object> array);
	
	public T nin(List<Object> array);

	public T exists(boolean exists);
}
