package gr.cite.femme.application.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import gr.cite.femme.core.dto.FemmeResponse;

@Provider
public class FemmeApplicationExceptionMapper implements ExceptionMapper<FemmeApplicationException> {

	@Override
	public Response toResponse(FemmeApplicationException exception) {
		Response.ResponseBuilder responseBuilder = Response.status(exception.getStatus());
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		
		femmeResponse.setStatus(exception.getStatus());
		femmeResponse.setMessage(exception.getMessage());
		
		if (exception.getCode() != null) {
			femmeResponse.setCode(exception.getCode());
		}
		
		return responseBuilder.entity(femmeResponse).type(MediaType.APPLICATION_JSON).build();
	}

}
