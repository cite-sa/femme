package gr.cite.femme.query.criteria;

public interface WhereBuilder<T> {

	Where<T> or();

	Where<T> and();

	CriteriaQuery<T> build();

}
