package to.us.resume_builder.spring.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

/**
 * Provides basic methods needed by a other Controllers.
 * 
 * @author Micah
 */
public abstract class BasicController {
	/**
	 * Creates a basic http 'OK' response builder, ready to have details added
	 * 
	 * @return A builder, ready to build a standard OK response.
	 */
	protected BodyBuilder getOkResponse() {
		return ResponseEntity.ok().headers(new HttpHeaders());
	}

	protected BodyBuilder getInternalServerErrorResponse(String errorMessage) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(new HttpHeaders()).header("error", errorMessage);
	}
}
