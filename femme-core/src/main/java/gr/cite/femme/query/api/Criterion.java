package gr.cite.femme.query.api;

public interface Criterion {
	
	public <T extends Criterion> Operator<T> root();
	
}
