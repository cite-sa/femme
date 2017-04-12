package gr.cite.femme.core.query.api;

public interface Criterion {
	
	public Operator<? extends Criterion> root();
	
}
