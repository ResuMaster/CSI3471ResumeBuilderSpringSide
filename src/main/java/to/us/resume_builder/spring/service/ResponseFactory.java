package to.us.resume_builder.spring.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

/**
 * Creates basic responses used to communicate with the user
 * 
 * @author Jacob Curtis
 * @author Micah Schiewe
 */
public class ResponseFactory {

    private static ResponseFactory fac;
    private static final Object LOCK = new Object();

    public static ResponseFactory getFactory() {
        if (fac == null) {
            synchronized (LOCK) {
                if (fac == null)
                    fac = new ResponseFactory();
            }
        }
        return fac;
    }

    /**
     * Creates a basic http 'OK' response builder, ready to have details added
     * 
     * @return A builder, ready to build a standard OK response.
     */
    protected BodyBuilder createOk() {
        return ResponseEntity.ok().headers(new HttpHeaders());
    }

    /**
     * Creates a basic http error response builder with a specified message.
     *
     * @param status       The error status
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
    protected BodyBuilder createInternalServerError(String errorMessage) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    /**
     * Creates a request timeout error response.
     *
     * @param errorMessage The error message to be specified in the header
     * @return The builder, ready to build the response.
     */
    protected BodyBuilder createRequestTimeout(String errorMessage) {
        return error(HttpStatus.REQUEST_TIMEOUT, errorMessage);
    }

    /**
     * Creates a bad request error response.
     *
     * @param errorMessage The error message to be specified in the header
     * @return The builder, ready to build the response.
     */
    protected BodyBuilder createBadRequest(String errorMessage) {
        return error(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * Creates a service unavailable error response.
     *
     * @param errorMessage The error message to be specified in the header
     * @return The builder, ready to build the response.
     */
    protected BodyBuilder createServiceUnavailable(String errorMessage) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
    }
}
