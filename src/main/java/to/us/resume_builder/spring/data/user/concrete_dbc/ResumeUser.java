package to.us.resume_builder.spring.data.user.concrete_dbc;

public class ResumeUser {
	private transient final String name;
	private transient final long hash;

	public ResumeUser(String name, long hash) {
		this.name = name;
		this.hash = hash;
	}

	/**
	 * Provides access to the user's name.
	 * 
	 * @return This user's username
	 */
	public String getName() {
		return name;
	}

	/**
	 * Determines whether the specified hash matches this user
	 * 
	 * @return Whether the suggested hash matches this user's hash.
	 */
	public boolean userIDMatches(long otherHash) {
		return otherHash == hash;
	}
}
