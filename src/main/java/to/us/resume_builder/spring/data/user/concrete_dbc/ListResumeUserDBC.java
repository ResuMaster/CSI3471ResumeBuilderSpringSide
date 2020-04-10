package to.us.resume_builder.spring.data.user.concrete_dbc;

import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;

import to.us.resume_builder.spring.data.user.abstract_dbc.DeleteResult;
import to.us.resume_builder.spring.data.user.abstract_dbc.ResumeUserDBC;
import to.us.resume_builder.spring.data.user.abstract_dbc.UserAlreadyExistsException;

/**
 * Placeholder class, connects to an internal List<User> as a database.
 * 
 * @author Micah
 */
public class ListResumeUserDBC implements ResumeUserDBC {
	private static final Logger LOG = Logger.getLogger(ListResumeUserDBC.class.getName());

	/** Placeholder user database */
	private static final Set<ResumeUser> users = new HashSet<ResumeUser>();

	@Override
	public boolean getUserExists(String name) {
		boolean result = getUser(name) != null;
		LOG.info("Query for " + name + ": " + result);
		return result;
	}

	@Override
	public long addUserToDB(String name) throws UserAlreadyExistsException {
		// Ensure we're not overwriting a user
		if (getUserExists(name)) {
			LOG.error("Attempting to create already-extant user " + name);
			throw new UserAlreadyExistsException(name);
		}

		// Create user
		long id = generateID();
		users.add(new ResumeUser(name, id));
		LOG.info("Add " + name);
		return id;
	}

	@Override
	public DeleteResult deleteUserFromDB(String name, long deleteHash) {
		ResumeUser u = getUser(name);

		// If user DNE, then return that it does not exist
		if (u == null)
			return DeleteResult.DOES_NOT_EXIST;

		// If provided hash wrong, return that the delete failed
		if (!u.userIDMatches(deleteHash))
			return DeleteResult.FAILURE;

		// Delete criteria met, delete user
		users.remove(u);
		u = null;
		return DeleteResult.SUCCESS;
	}

	@Override
	public boolean getUserValid(String userName, long userID) {
		ResumeUser u = getUser(userName);
		return u != null && u.userIDMatches(userID);
	}

	/**
	 * Gets a user by name from the database
	 * 
	 * @param name The name of the user
	 * @return The user with the specified name, or null if it does not exist.
	 */
	private ResumeUser getUser(String name) {
		return users.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Generates a unique ID TODO make it unique. Somehow.
	 * 
	 * @param input The string to hash
	 * @return A random ID.
	 */
	private long generateID() {
		long l;
		do {
			l = (long) (Math.random() * 100_000_000_000_000_000l);
		} while (l == INSERT_FAIL);
		return l;
	}
}