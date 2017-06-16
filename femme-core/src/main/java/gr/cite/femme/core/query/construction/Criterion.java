package gr.cite.femme.core.query.construction;

public interface Criterion {
	
	public Operator<? extends Criterion> root();
	
}
