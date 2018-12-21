package gr.cite.femme.core.query.construction;

import java.util.List;

public interface Query<U extends Criterion> {
	Query<U> addCriterion(U criterion);
	List<U> getCriteria();
}
