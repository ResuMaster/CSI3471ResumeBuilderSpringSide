package to.us.resume_builder.spring.data.pdf;

import to.us.resume_builder.spring.data.pdf.abstract_dbc.PdfDBC;
import to.us.resume_builder.spring.data.pdf.concrete_dbc.MapPdfDBC;

/**
 * Provides access to the PdfDBC. Hides the type of this class from callers, to
 * lower coupling between users of the DBC and the DBC implementation.
 * 
 * @author Micah
 */
public class PdfDBCHandle {
	private static final Object LOCK = new Object();
	private static PdfDBC state;

	/**
	 * Provide access to the singleton PdfDBC
	 * 
	 * @return The connection to the user database
	 */
	public static PdfDBC getPdfDBC() {
		if (state == null) {
			synchronized (LOCK) {
				if (state == null)
					state = new MapPdfDBC();
			}
		}

		return state;
	}
}
