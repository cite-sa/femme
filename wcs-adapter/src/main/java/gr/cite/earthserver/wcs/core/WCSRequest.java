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
		if (mediaType.toString().equals("image/png")) {
			BufferedImage image = null;
			try {
				
//				image = ImageIO.read(new URL("http://access.planetserver.eu:8080/rasdaman/ows?service=WCS&version=2.0.1&request=ProcessCoverages&query=for%20data%20in%20(frt0000cc22_07_if165l_trr3)%20return%20encode(%20%7B%20red:%20(int)(255%20/%20(max((data.band_233%20!=%2065535)%20*%20data.band_233)%20-%20min(data.band_233)))%20*%20(data.band_233%20-%20min(data.band_233));%20green:%20(int)(255%20/%20(max((data.band_13%20!=%2065535)%20*%20data.band_13)%20-%20min(data.band_13)))%20*%20(data.band_13%20-%20min(data.band_13));%20blue:%20(int)(255%20/%20(max((data.band_78%20!=%2065535)%20*%20data.band_78)%20-%20min(data.band_78)))%20*%20(data.band_78%20-%20min(data.band_78))%20;%20alpha:%20(data.band_100%20!=%2065535)%20*%20255%7D,%20%22png%22,%20%22nodata=null%22)"));
				image = ImageIO.read(webTarget.getUri().toURL());
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    try {
				ImageIO.write(image, "png", baos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    responseString = Base64.getEncoder().encodeToString(baos.toByteArray());
		} else {
			responseString = response.readEntity(String.class);			
		}
		

		if (response.getStatus() >= 300) {
			logger.error(endpoint + ":" + responseString);
			throw new WCSRequestException(endpoint + ":" + responseString, response.getStatus());
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
