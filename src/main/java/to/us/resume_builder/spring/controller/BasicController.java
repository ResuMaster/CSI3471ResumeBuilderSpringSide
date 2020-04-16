package to.us.resume_builder.spring.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

/**
 * Provides basic methods needed by a other Controllers.
 * 
 * @author Micah Schiewe
 * @author Jacob Curtis
 */
public abstract class BasicController {
	/**
	 * Creates a basic http 'OK' response builder, ready to have details added
	 * 
	 * @return A builder, ready to build a standard OK response.
	 */
	protected BodyBuilder ok() {
		return ResponseEntity.ok().headers(new HttpHeaders());
	}

	/**
	 * Creates a basic http error response builder with a specified message.
	 *
	 * @param status The error status
	 * @param errorMessage The error message to be specified in the header
	 * @return The builder, ready to build the response.
	 */
	protected BodyBuilder error(HttpStatus status, String errorMessage) {
		return ResponseEntity.status(status).headers(new HttpHeaders()).header("error", errorMessage);
	}

	/**
	 * Creates an internal server error response.
	 *
	 * @param errorMessage The error message to be specified in the header
	 * @return The builder, ready to build the response.
	 */
	protected BodyBuilder internalServerError(String errorMessage) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
	}

	/**
	 * Creates a request timeout error response.
	 *
	 * @param errorMessage The error message to be specified in the header
	 * @return The builder, ready to build the response.
	 */
	protected BodyBuilder requestTimeout(String errorMessage) {
		return error(HttpStatus.REQUEST_TIMEOUT, errorMessage);
	}

	/**
	 * Creates a bad request error response.
	 *
	 * @param errorMessage The error message to be specified in the header
	 * @return The builder, ready to build the response.
	 */
	protected BodyBuilder badRequest(String errorMessage) {
		return error(HttpStatus.BAD_REQUEST, errorMessage);
	}

	/**
	 * Creates a service unavailable error response.
	 *
	 * @param errorMessage The error message to be specified in the header
	 * @return The builder, ready to build the response.
	 */
	protected BodyBuilder serviceUnavailable(String errorMessage) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
	}
}
