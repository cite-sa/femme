package gr.cite.earthserver.wcs.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.earthserver.wcs.core.WCSResponse;

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
		
		String responseString = "";
		if ("image/png".equals(mediaType.toString())) {
			BufferedImage image = null;
			try {
				image = ImageIO.read(webTarget.getUri().toURL());
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    try {
				ImageIO.write(image, "png", baos);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		    responseString = Base64.getEncoder().encodeToString(baos.toByteArray());
		} else {
			responseString = response.readEntity(String.class);			
		}
		

		if (response.getStatus() >= 300) {
			/*logger.error(endpoint + ":" + responseString);*/
			throw new WCSRequestException(endpoint + ": " + responseString, response.getStatus());
		}

		// TODO Content-type:
		// multipart/x-mixed-replace;boundary=End
		/*logger.warn("-----------------------------------------------------------------------");
		logger.warn("-----------------------------------------------------------------------");
		logger.warn("--  TODO  read Content-type: multipart/x-mixed-replace;boundary=End  --");
		logger.warn("-----------------------------------------------------------------------");
		logger.warn("-----------------------------------------------------------------------");*/
		// FIXME delete if
		if (responseString.startsWith("\r\n--End")) {
			responseString = responseString.replaceAll("--End--", "").replaceAll("--End", "")
					.replaceAll("Content-type: text/plain", "").trim();
		}

		return new WCSResponse(endpoint, mediaType, responseString);

	}

}
