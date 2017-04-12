package gr.cite.earthserver.wcs.adapter.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.femme.core.utils.Pair;

public class WCSAdapterElements {
	
	private WCSAdapterRequest request;
	
	private Multimap<String, String> andAttributes;
	
	private Multimap<String, String> orAttributes;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	public WCSAdapterElements() {
		andAttributes = ArrayListMultimap.create();
		orAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
	}
	
	public WCSAdapterElements(WCSAdapterRequest request) {
		andAttributes = ArrayListMultimap.create();
		orAttributes = ArrayListMultimap.create();
		and = false;
		or = false;
		
		this.request = request;
	}
	
	public WCSAdapterElements and() {
		and = true;
		or = false;
		
		return this;
	}
	
	public WCSAdapterElements or() {
		and = false;
		or = true;
		
		return this;
	}
	
	public WCSAdapterElements attribute(Pair<String, String> attribute) {
		
		if (and) {
			andAttributes.put(attribute.getLeft(), attribute.getRight());
		} else if (or) {
			orAttributes.put(attribute.getLeft(), attribute.getRight());
		}
		
		return this;
	}
	
	public WCSAdapterElements end() {
		and = false;
		or = false;
		
		return this;
	}
	
	public WCSAdapterElements xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public WCSAdapterRequest getRequest() {
		return request;
	}

	public void setRequest(WCSAdapterRequest request) {
		this.request = request;
	}

	public Multimap<String, String> getAndServersAttributes() {
		return andAttributes;
	}

	public void setAndServersAttributes(Multimap<String, String> andServersAttributes) {
		this.andAttributes = andServersAttributes;
	}

	public Multimap<String, String> getOrServersAttributes() {
		return orAttributes;
	}

	public void setOrServersAttributes(Multimap<String, String> orServersAttributes) {
		this.orAttributes = orServersAttributes;
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
