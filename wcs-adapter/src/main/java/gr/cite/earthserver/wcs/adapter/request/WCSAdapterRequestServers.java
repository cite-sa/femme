package gr.cite.earthserver.wcs.adapter.request;

import java.util.Map;

public class WCSAdapterRequestServers {
	
	private WCSAdapterRequest request;
	
	private Map<String, String> serversProperties;
	
	private boolean and;
	
	private boolean or;
	
	private String xPath;
	
	public WCSAdapterRequestServers() {
		
	}
	
	public WCSAdapterRequestServers(WCSAdapterRequest request) {
		this.request = request;
	}
	
	public WCSAdapterRequestServers and(Map<String, String> serversProperties) {
		this.serversProperties = serversProperties;
		and = true;
		or = false;
		return this;
	}
	
	public WCSAdapterRequestServers or(Map<String, String> serversProperties) {
		this.serversProperties = serversProperties;
		and = false;
		or = true;
		return this;
	}
	
	public WCSAdapterRequestServers xPath(String xPath) {
		this.xPath = xPath;
		return this;
	}
	
	public WCSAdapterCoverages coverages() {
		return new WCSAdapterCoverages(request);
	}
	
}
