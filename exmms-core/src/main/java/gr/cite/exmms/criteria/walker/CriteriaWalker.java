package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.serializer.CriteriaQuerySerializer;

public class CriteriaWalker<T> {
	CriteriaQuerySerializer<T> querySerializer;

	CriteriaQuery<T> datastoreQuery;
	
	public void walk() {
		querySerializer.whereBuilder();
		datastoreQuery.whereBuilder();
	}
}
