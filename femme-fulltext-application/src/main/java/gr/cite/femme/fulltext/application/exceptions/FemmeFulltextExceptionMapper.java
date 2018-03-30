package gr.cite.femme.fulltext.application.exceptions;

import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FemmeFulltextExceptionMapper implements ExceptionMapper<FemmeFulltextException> {
	private static final Logger logger = LoggerFactory.getLogger(FemmeFulltextExceptionMapper.class);

	@Override
	public Response toResponse(FemmeFulltextException exception) {
		logger.error(exception.getMessage(), exception);
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		
		femmeResponse.setStatus(exception.getStatus());
		femmeResponse.setMessage(exception.getMessage());

		/*StringWriter errorStackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(errorStackTrace));
		femmeResponse.setDeveloperMessage(errorStackTrace.toString());*/
		if (exception.getCause() != null) {
			femmeResponse.setDeveloperMessage(exception.getCause().getMessage());
		}

		femmeResponse.setCode(exception.getCode());

		return Response.status(femmeResponse.getStatus()).entity(femmeResponse).type(MediaType.APPLICATION_JSON).build();
	}

}
