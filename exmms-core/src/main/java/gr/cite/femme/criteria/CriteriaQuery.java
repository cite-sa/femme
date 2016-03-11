package gr.cite.femme.criteria;

import java.util.List;

public interface CriteriaQuery<T> {
	
	Where<T> whereBuilder();

	/**
	 * 
	 * @param <S> expression factory of type S
	 */
	<S> Where<S> expressionFactory();

	T find(String id);
	
	T find(T t);

	List<T> find();

}
