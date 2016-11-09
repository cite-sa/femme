package gr.cite.femme.application.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import gr.cite.femme.dto.FemmeResponse;

public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		
		femmeResponse.setStatus(getHttpStatus(exception));
		femmeResponse.setMessage(exception.getMessage());
		
		StringWriter errorStackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(errorStackTrace));
		femmeResponse.setDeveloperMessage(errorStackTrace.toString());

		return Response.status(femmeResponse.getStatus())
				.entity(femmeResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	private Integer getHttpStatus(Throwable exception) {
		if(exception instanceof WebApplicationException) {
			return ((WebApplicationException) exception).getResponse().getStatus();
		} else {
			return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
		}
	}
}
