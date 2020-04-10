package to.us.resume_builder.spring.data.user.abstract_dbc;

/**
 * Interface for connecting to the ResumeUser database.
 * 
 * @author Micah
 */
public interface ResumeUserDBC {

	/** The code returned by a failed insert method. May not be used as an ID. */
	public static final long INSERT_FAIL = 0;

	/**
	 * Determines whether there is a user by the specified name in the database.
	 * 
	 * @param name The name of the user to check on.
	 * @return Whether or not the user exists in the database.
	 */
	public boolean getUserExists(String name);

	/**
	 * Adds the user into the database.
	 * 
	 * @param name The name of the new user
	 * @return The hash for the resulting user, or INSERT_FAILED if no user could be
	 *         added.
	 */
	public long addUserToDB(String name) throws UserAlreadyExistsException;

	/**
	 * Removes the specified user from the database, if its information matches.
	 * 
	 * @param name The name of the user to remove
	 * @param id   The id assigned to the user to delete
	 * @return The deletion outcome; a success, failure, or attempt to delete
	 *         non-existent user.
	 */
	public DeleteResult deleteUserFromDB(String name, long id);

	/**
	 * Ensures that the specified user name and id go together
	 * 
	 * @param userName The name to check
	 * @param userID   The ID to check
	 * @return Whether or not this information describes a valid user.
	 */
	public boolean getUserValid(String userName, long userID);
}
