package gr.cite.femme.criteria.serializer;

import java.util.List;

import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.Where;

public class CriteriaQuerySerializer<T> implements CriteriaQuery<T> {

	private WhereSerializer<T> where = new WhereSerializer<>();

	@Override
	public Where<T> whereBuilder() {
		return where;
	}

	@Override
	public <S> Where<S> expressionFactory() {
		return new WhereSerializer<>(true);
	}

	@Override
	public T find(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> find(T t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> find() {
		// TODO Auto-generated method stub
		return null;
	}

	public WhereSerializer<T> getWhere() {
		return where;
	}

	public void setWhere(WhereSerializer<T> where) {
		this.where = where;
	}

}
