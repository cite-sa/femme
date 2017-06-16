package gr.cite.femme.core.query.construction;

import java.util.List;

public interface LogicalOperator<T extends Criterion> {
	
	public void or(List<T> criteria);
	
	public void and(List<T> criteria);
	
	public void not(List<T> criteria);
	
	public void nor(List<T> criteria);

}
