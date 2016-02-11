package gr.cite.exmms.criteria;

public interface WhereBuilder<T> {

	Where<T> or();

	Where<T> and();

	CriteriaQuery<T> build();

}
