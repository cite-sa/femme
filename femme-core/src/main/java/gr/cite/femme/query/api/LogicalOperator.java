package gr.cite.femme.query.api;

import java.util.List;

public interface LogicalOperator<T extends Criterion> {
	
	public void or(List<T> criteria);
	
	public void and(List<T> criteria);
	
	public void not(List<T> criteria);
	
	public void nor(List<T> criteria);

}
