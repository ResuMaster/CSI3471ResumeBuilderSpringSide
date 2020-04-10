package to.us.resume_builder.spring.data.user;

import to.us.resume_builder.spring.data.user.abstract_dbc.ResumeUserDBC;
import to.us.resume_builder.spring.data.user.concrete_dbc.ListResumeUserDBC;

/**
 * Provides access to the ResumeUserDBC. Hides the type of this class from
 * callers, to lower coupling between users of the DBC and the DBC
 * implementation.
 * 
 * @author Micah
 *
 */
public class ResumeUserDBCHandle {
	private static final Object LOCK = new Object();
	private static ResumeUserDBC state;

	/**
	 * Provide access to the singleton UserDBC
	 * 
	 * @return The connection to the user database
	 */
	public static ResumeUserDBC getUserDBC() {
		if (state == null) {
			synchronized (LOCK) {
				if (state == null)
					state = new ListResumeUserDBC();
			}
		}

		return state;
	}
}
