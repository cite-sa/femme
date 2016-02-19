package gr.cite.exmms.criteria.serializer;

import java.util.List;

import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.Where;

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
	public T find(T t) {
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
