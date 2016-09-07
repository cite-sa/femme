package gr.cite.femme.query.api;

import java.util.List;

public interface Query<T extends CriterionInterface> {
	
	public Query<T> addCriterion(T criterion);
	
	public List<T> getCriteria();
	
}
