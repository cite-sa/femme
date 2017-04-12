package gr.cite.earthserver.wcs.adapter.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.femme.core.utils.Pair;

public class WCSAdapterCoverages {
	
	private WCSAdapterRequest request;
	
	private Multimap<String, String> andCoverageAttributes;
	
	private Multimap<String, String> orCoverageAttributes;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	public WCSAdapterCoverages() {
		andCoverageAttributes = ArrayListMultimap.create();
		orCoverageAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
	}
	
	public WCSAdapterCoverages(WCSAdapterRequest request) {
		andCoverageAttributes = ArrayListMultimap.create();
		orCoverageAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
		
		this.request = request;
	}
	
	public WCSAdapterCoverages and() {
		and = true;
		or = false;
		
		return this;
	}
	
	public WCSAdapterCoverages or() {
		and = false;
		or = true;
		
		return this;
	}
	
	public WCSAdapterCoverages attribute(Pair<String, String> attribute) {
		
		if (and) {
			andCoverageAttributes.put(attribute.getLeft(), attribute.getRight());
		} else if (or) {
			orCoverageAttributes.put(attribute.getLeft(), attribute.getRight());
		}
		
		return this;
	}
	
	public WCSAdapterCoverages xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public WCSAdapterRequest getRequest() {
		return request;
	}

	public void setRequest(WCSAdapterRequest request) {
		this.request = request;
	}

	public Multimap<String, String> getAndCoverageAttributes() {
		return andCoverageAttributes;
	}

	public void setAndCoverageAttributes(Multimap<String, String> andCoverageAttributes) {
		this.andCoverageAttributes = andCoverageAttributes;
	}

	public Multimap<String, String> getOrCoverageAttributes() {
		return orCoverageAttributes;
	}

	public void setOrCoverageAttributes(Multimap<String, String> orCoverageAttributes) {
		this.orCoverageAttributes = orCoverageAttributes;
	}

	public boolean isAnd() {
		return and;
	}

	public void setAnd(boolean and) {
		this.and = and;
	}

	public boolean isOr() {
		return or;
	}

	public void setOr(boolean or) {
		this.or = or;
	}

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}
}
