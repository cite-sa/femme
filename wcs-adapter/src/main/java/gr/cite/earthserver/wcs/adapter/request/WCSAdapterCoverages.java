package gr.cite.earthserver.wcs.adapter.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.femme.client.query.QueryClient;

public class WCSAdapterCoverages {
	
	private WCSAdapterRequest request;
	
	private Multimap<String, String> coveragesProperties;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	
	public WCSAdapterCoverages() {
		coveragesProperties = ArrayListMultimap.create();
		and = false;
		or = false;
	}
	
	public WCSAdapterCoverages(WCSAdapterRequest request) {
		coveragesProperties = ArrayListMultimap.create();
		this.request = request;
		this.request.setCoverages(this);
	}
	
	protected WCSAdapterRequest getRequest() {
		return request;
	}

	protected void setRequest(WCSAdapterRequest request) {
		this.request = request;
	}

	protected Multimap<String, String> getCoveragesProperties() {
		return coveragesProperties;
	}

	protected void setCoveragesProperties(Multimap<String, String> coveragesProperties) {
		this.coveragesProperties = coveragesProperties;
	}

	protected boolean isAnd() {
		return and;
	}

	protected void setAnd(boolean and) {
		this.and = and;
	}

	protected boolean isOr() {
		return or;
	}

	protected void setOr(boolean or) {
		this.or = or;
	}

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public WCSAdapterCoverages and(Multimap<String, String> coveragesProperties) {
		this.coveragesProperties = coveragesProperties;
		and = true;
		or = false;
		return this;
	}
	
	public WCSAdapterCoverages or(Multimap<String, String> coveragesProperties) {
		this.coveragesProperties = coveragesProperties;
		and = false;
		or = true;
		return this;
	}
	
	public WCSAdapterCoverages xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public QueryClient get() {
		return request.mapToQuery();
	}
}
