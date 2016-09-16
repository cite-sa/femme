package gr.cite.earthserver.wcs.adapter.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.femme.client.query.QueryClient;

public class WCSAdapterServers {
	
	private WCSAdapterRequest request;
	
	private Multimap<String, String> serversProperties;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	public WCSAdapterServers() {
		serversProperties = ArrayListMultimap.create();
		and = false;
		or = false;
	}
	
	public WCSAdapterServers(WCSAdapterRequest request) {
		this.request = request;
	}
	
	public WCSAdapterRequest getRequest() {
		return request;
	}

	public void setRequest(WCSAdapterRequest request) {
		this.request = request;
	}

	public Multimap<String, String> getServersProperties() {
		return serversProperties;
	}

	public void setServersProperties(Multimap<String, String> serversProperties) {
		this.serversProperties = serversProperties;
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

	public WCSAdapterServers and(Multimap<String, String> serversProperties) {
		this.serversProperties = serversProperties;
		and = true;
		or = false;
		return this;
	}
	
	public WCSAdapterServers or(Multimap<String, String> serversProperties) {
		this.serversProperties = serversProperties;
		and = false;
		or = true;
		return this;
	}
	
	public WCSAdapterServers xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public WCSAdapterCoverages coverages() {
		return new WCSAdapterCoverages(request);
	}
	
	public QueryClient get() {
		return request.mapToQuery();
	}
}
