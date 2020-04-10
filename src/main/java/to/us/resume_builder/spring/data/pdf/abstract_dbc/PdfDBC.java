package to.us.resume_builder.spring.data.pdf.abstract_dbc;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;

import to.us.resume_builder.spring.data.user.abstract_dbc.DeleteResult;

/**
 * Interface for connecting to the PDF database.
 * 
 * @author Micah
 */
public interface PdfDBC {
	/**
	 * Gets the PDF specified by the name and id, if it exists
	 * 
	 * @param name  The user who owns the pdf
	 * @param pdfID The ID of the pdf
	 * @return A stream containing the pdf, or null if there was no pdf.
	 */
	public ByteArrayInputStream getPDF(String name, long pdfID);

	/**
	 * 
	 * @param name The user who owns the pdf.
	 * @param pdf  A stream containing the PDF to upload.
	 * @return The pdfID for the newly-created PDF.
	 */
	public long addPdf(String name, InputStreamResource pdf);

	/**
	 * Removes the specified pdf from the database, if it can be found.
	 * 
	 * @param name The name of the pdf's owner
	 * @param id   The id assigned to the pdf to delete
	 * @return The deletion outcome; a success, failure, or attempt to delete
	 *         non-existent pdf.
	 */
	public DeleteResult deletePdfFromDB(String name, long pdfID);
}
