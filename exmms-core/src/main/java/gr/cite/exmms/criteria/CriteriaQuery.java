package gr.cite.exmms.criteria;

import java.util.List;

public interface CriteriaQuery<T> {
	
	Where<T> whereBuilder();

	Where<T> expressionFactory();

	T find(String id);
	
	T find(T t);

	List<T> find();

}
