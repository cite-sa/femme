package gr.cite.femme.query.mongodb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gr.cite.femme.query.IWhere;

public class Where implements IWhere<Criteria> {
	Criteria criteria;

	public Where() {
		
	}
	
	public Where(Criteria criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public Criteria eq(Object value) {
		criteria.setValue(value);
		return criteria;
	}

	@Override
	public Criteria gt(Object value) {
		Map<String, Object> gtMap = new LinkedHashMap<>();
		gtMap.put("$gt", value);
		criteria.setValue(gtMap);
		return criteria;
	}

	@Override
	public Criteria gte(Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("$gte", value);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria lt(Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("$lt", value);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria lte(Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("$lte", value);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria ne(Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("$ne", value);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria in(List<Object> array) {
		Map<String, List<Object>> map = new LinkedHashMap<>();
		map.put("$in", array);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria nin(List<Object> array) {
		Map<String, List<Object>> map = new LinkedHashMap<>();
		map.put("$nin", array);
		criteria.setValue(map);
		return criteria;
	}

	@Override
	public Criteria exists(boolean exists) {
		// TODO Auto-generated method stub
		return null;
	}

}
