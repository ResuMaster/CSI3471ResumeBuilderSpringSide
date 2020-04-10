package to.us.resume_builder.spring.data.user.abstract_dbc;

public class UserAlreadyExistsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception with a distinct message.
	 * 
	 * @param name The name of the user that could not be inserted.
	 */
	public UserAlreadyExistsException(String name) {
		super("The specified username " + name + " already exists!");
	}
}
