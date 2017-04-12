package gr.cite.earthserver.wcs.adapter.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.femme.core.utils.Pair;

public class WCSAdapterServers {
	
	private WCSAdapterRequest request;
	
	private Multimap<String, String> andServerAttributes;
	
	private Multimap<String, String> orServerAttributes;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	public WCSAdapterServers() {
		andServerAttributes = ArrayListMultimap.create();
		orServerAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
	}
	
	public WCSAdapterServers(WCSAdapterRequest request) {
		andServerAttributes = ArrayListMultimap.create();
		orServerAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
		
		this.request = request;
	}
	
	public WCSAdapterServers and() {
		and = true;
		or = false;
		
		return this;
	}
	
	public WCSAdapterServers or() {
		and = false;
		or = true;
		
		return this;
	}
	
	public WCSAdapterServers attribute(Pair<String, String> attribute) {
		
		if (and) {
			andServerAttributes.put(attribute.getLeft(), attribute.getRight());
		} else if (or) {
			orServerAttributes.put(attribute.getLeft(), attribute.getRight());
		}
		
		return this;
	}
	
	public WCSAdapterServers xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public WCSAdapterRequest getRequest() {
		return request;
	}

	public void setRequest(WCSAdapterRequest request) {
		this.request = request;
	}

	public Multimap<String, String> getAndServerAttributes() {
		return andServerAttributes;
	}

	public void setAndServerAttributes(Multimap<String, String> andServerAttributes) {
		this.andServerAttributes = andServerAttributes;
	}

	public Multimap<String, String> getOrServerAttributes() {
		return orServerAttributes;
	}

	public void setOrServerAttributes(Multimap<String, String> andServerAttributes) {
		this.orServerAttributes = andServerAttributes;
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
