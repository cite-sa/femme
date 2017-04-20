package gr.cite.femme.application.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import gr.cite.femme.core.dto.FemmeResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

@Provider
public class FemmeApplicationExceptionMapper implements ExceptionMapper<FemmeApplicationException> {

	@Override
	public Response toResponse(FemmeApplicationException exception) {
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
