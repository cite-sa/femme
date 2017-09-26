package gr.cite.earthserver.wcs.core;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSParseUtils;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriComponent.Type;

import java.util.List;

public class WCSRequestBuilder {
	private String endpoint;

	private String version = "2.0.1";

	public WCSRequestBuilder endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	/**
	 * Default value is "2.0.1"
	 * 
	 * @param version
	 * @return
	 */
	public WCSRequestBuilder version(String version) {
		this.version = version;
		return this;
	}

	public ProcessCoverages processCoverages() {
		return new ProcessCoverages(build());
	}

	public GetCapabilities getCapabilities() {
		return new GetCapabilities(build());
	}

	public DescribeCoverage describeCoverage() {
		return new DescribeCoverage(build());
	}

	public GetCoverage getCoverage() {
		return new GetCoverage(build());
	}

	private WebTarget build() {
		return ClientBuilder.newClient().target(endpoint).queryParam("service", "WCS").queryParam("version", version);
	}

	public static class ProcessCoverages {

		private WebTarget webTarget;

		private String query;
		private String wcpsVersion = "1.0";

		public ProcessCoverages(WebTarget build) {
			this.webTarget = build.queryParam("request", "ProcessCoverages");
		}

		public ProcessCoverages wcpsVersion(String wcpsVersion) {
			this.wcpsVersion = wcpsVersion;
			return this;
		}

		public ProcessCoverages query(String query) {
			this.query = query;
			return this;
		}

		public WCSRequest build() {
			return new WCSRequest(this.webTarget/*.queryParam("wcpsVersion", wcpsVersion)*/.queryParam("query",
					UriComponent.encode(query, Type.QUERY_PARAM_SPACE_ENCODED)));
		}

	}

	public static class GetCapabilities {

		private WebTarget webTarget;

		public GetCapabilities(WebTarget build) {
			this.webTarget = build.queryParam("request", "GetCapabilities");
		}

		public WCSRequest build() {
			return new WCSRequest(webTarget);
		}
	}

	public static class DescribeCoverage {

		private WebTarget webTarget;

		private String coverageId;

		DescribeCoverage(WebTarget webTarget) {
			this.webTarget = webTarget.queryParam("request", "DescribeCoverage");
		}

		public DescribeCoverage coverageId(String coverageId) {
			this.coverageId = coverageId;
			return this;
		}

		public WCSRequest build() {
			return new WCSRequest(webTarget.queryParam("coverageId", coverageId));
		}
	}

	public static class GetCoverage {

		private WebTarget webTarget;

		private String coverageId;

		GetCoverage(WebTarget webTarget) {
			this.webTarget = webTarget.queryParam("request", "GetCoverage");
		}

		public GetCoverage coverageId(String coverageId) {
			this.coverageId = coverageId;
			return this;
		}

		public WCSRequest build() {
			return new WCSRequest(webTarget.queryParam("coverageId", coverageId));
		}
	}

	public static void main(String[] args) throws WCSRequestException, ParseException {
		WCSRequestBuilder wcsRequestBuilder = new WCSRequestBuilder().endpoint("http://access.planetserver.eu:8080/rasdaman/ows");
		WCSResponse getCapabilities = wcsRequestBuilder.getCapabilities().build().get();
		System.out.println(WCSParseUtils.getCoverageIds(getCapabilities.getResponse()).size());

	}
}
