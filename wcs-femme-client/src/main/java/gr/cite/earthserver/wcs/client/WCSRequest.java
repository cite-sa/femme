package gr.cite.earthserver.wcs.client;

import java.net.URI;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WCSRequest {
	private static final Logger logger = LoggerFactory.getLogger(WCSRequest.class);

	public static WCSRequestBuilder newBuilder() {
		return new WCSRequestBuilder();
	}

	private WebTarget webTarget;

	WCSRequest(WebTarget webTarget) {
		this.webTarget = webTarget;
	}

	public WCSResponse get() throws WCSRequestException {

		Response response = webTarget.request().get();

		String endpoint = webTarget.getUri().toString();
		MediaType mediaType = response.getMediaType();
		String responseString = response.readEntity(String.class);

		if (response.getStatus() >= 300) {
			throw new WCSRequestException(responseString, response.getStatus());
		}

		// TODO Content-type:
		// multipart/x-mixed-replace;boundary=End
		logger.warn("-----------------------------------------------------------------------");
		logger.warn("-----------------------------------------------------------------------");
		logger.warn("--  TODO  read Content-type: multipart/x-mixed-replace;boundary=End  --");
		logger.warn("-----------------------------------------------------------------------");
		logger.warn("-----------------------------------------------------------------------");
		// FIXME delete if
		if (responseString.startsWith("\r\n--End")) {
			responseString = responseString.replaceAll("--End--", "").replaceAll("--End", "")
					.replaceAll("Content-type: text/plain", "").trim();
		}

		return new WCSResponse(endpoint, mediaType, responseString);

	}

}
