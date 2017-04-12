package gr.cite.femme.application.exception;

import gr.cite.femme.core.dto.FemmeResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BeanValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		ConstraintViolation cv = (ConstraintViolation) exception.getConstraintViolations().toArray()[0];
		//System.out.println(cv.getMessage());

		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		femmeResponse.setStatus(Response.Status.BAD_REQUEST.getStatusCode()).setMessage(cv.getMessage());

		return Response.status(Response.Status.BAD_REQUEST)
				.entity(femmeResponse)
				.type(MediaType.APPLICATION_JSON).build();
	}
}
