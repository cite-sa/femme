package gr.cite.femme.query.api;

import java.util.List;

public interface InclusionOperator<T extends Criterion> {
	
	public void inCollections(List<T> criteria);
	
	public void inAnyCollection(List<T> criteria);
	
	public void hasDataElements(List<T> criteria);
	
	public void hasAnyDataElement(List<T> criteria);
	
}
