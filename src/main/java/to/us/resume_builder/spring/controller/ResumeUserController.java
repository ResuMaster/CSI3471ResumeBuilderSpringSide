package to.us.resume_builder.spring.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import to.us.resume_builder.spring.data.user.ResumeUserDBCHandle;
import to.us.resume_builder.spring.data.user.abstract_dbc.DeleteResult;
import to.us.resume_builder.spring.data.user.abstract_dbc.ResumeUserDBC;
import to.us.resume_builder.spring.data.user.abstract_dbc.UserAlreadyExistsException;

/**
 * Class that offers connection to the User database from an outside program.
 * 
 * @author Micah
 */
@RestController
public class ResumeUserController extends BasicController {

	private static final Logger LOG = Logger.getLogger(ResumeUserController.class.getName());

	// TODO autowire this
	private ResumeUserDBC dbc = ResumeUserDBCHandle.getUserDBC();

	/**
	 * Endpoint for adding the user to the provided DB
	 * 
	 * @param newHash The hash for the new user to add
	 * @return The ID of the newly-created user. This value acts as a special
	 *         password for the user; it must be kept hidden by the client program.
	 */
	@PutMapping("/user")
	public ResponseEntity<Long> addUser(@RequestParam(name = "name", required = true) String name) {
		try {
			var result = getOkResponse().body(dbc.addUserToDB(name));
			return result;
		} catch (UserAlreadyExistsException e) {
			logError("User already existed in database", "addUser", e);
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Sees if the specified user is registered to the system.
	 * 
	 * @param name The username to check whether exists in the system.
	 * @return Whether the user already exists.
	 */
	@GetMapping("/user")
	public ResponseEntity<Boolean> hasUser(@RequestParam(name = "name", required = true) String name) {
		return getOkResponse().body(dbc.getUserExists(name));
	}

	/**
	 * Deletes the user with the specified name and id, if possible.
	 * 
	 * @param name The name of the user to delete
	 * @param id   The ID of the user to delete.
	 * @return The outcome of the deletion operation.
	 */
	@DeleteMapping("/user")
	public ResponseEntity<DeleteResult> deleteUser(@RequestParam(name = "name", required = true) String name,
			@RequestParam(name = "id", required = true) long id) {
		return getOkResponse().body(dbc.deleteUserFromDB(name, id));
	}

	/**
	 * Logs a SEVERE message warning of the thrown exception.
	 * 
	 * @param msg    Error message
	 * @param method Method logged from
	 * @param error  The throwable that caused the error
	 */
	private static final void logError(String msg, String method, Throwable error) {
		LOG.logp(Level.SEVERE, ResumeUserController.class.getName(), method, msg, error);
	}
}
