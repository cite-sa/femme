package gr.cite.exmms.manager.criteria;

public interface WhereBuilder<T> {

	Where<T> or();

	Where<T> and();

	CriteriaQuery<T> build();

}
