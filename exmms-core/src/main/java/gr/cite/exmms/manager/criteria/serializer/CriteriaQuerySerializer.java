package gr.cite.exmms.manager.criteria.serializer;

import java.util.List;

import gr.cite.exmms.manager.criteria.CriteriaQuery;
import gr.cite.exmms.manager.criteria.Where;

public class CriteriaQuerySerializer<T> implements CriteriaQuery<T> {

	private Where<T> where = new WhereSerializer<>();

	@Override
	public Where<T> whereBuilder() {
		return where;
	}

	@Override
	public Where<T> expressionFactory() {
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

	public Where<T> getWhere() {
		return where;
	}

	public void setWhere(Where<T> where) {
		this.where = where;
	}

}
