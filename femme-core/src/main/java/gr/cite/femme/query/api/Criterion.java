package gr.cite.femme.query.api;

public interface CriterionInterface {
	
	public <T extends CriterionInterface> Operator<T> root();
	
}
