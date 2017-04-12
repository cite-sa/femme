package gr.cite.femme.core.query.api;

import java.util.List;

public interface Query<T extends Criterion> {
	
	public Query<T> addCriterion(T criterion);
	
	public List<T> getCriteria();
	
}
