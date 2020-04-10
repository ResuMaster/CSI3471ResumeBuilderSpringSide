package to.us.resume_builder.spring.controller;

import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import to.us.resume_builder.spring.data.pdf.PdfDBCHandle;
import to.us.resume_builder.spring.data.pdf.abstract_dbc.PdfDBC;
import to.us.resume_builder.spring.data.user.ResumeUserDBCHandle;
import to.us.resume_builder.spring.data.user.abstract_dbc.DeleteResult;
import to.us.resume_builder.spring.data.user.abstract_dbc.ResumeUserDBC;

@RestController
public class PDFController extends BasicController {
	private static final Logger LOG = Logger.getLogger(ResumeUserController.class.getName());

	// TODO autowire
	private PdfDBC dbc;

	@GetMapping(name = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> getPdf(@RequestParam(name = "user", required = true) String user,
			@RequestParam(name = "pdfID", required = true) long pdfID) {
		var pdf = dbc.getPDF(user, pdfID);

		// If the PDF doesn't exist, then return a bad request
		if (pdf == null)
			return ResponseEntity.notFound().build();

		// Return the pdf
		return getOkResponse().contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(pdf));
	}

	@PutMapping(name = "/pdf")
	public ResponseEntity<Long> putPdf(@RequestParam(name = "user", required = true) String user,
			@RequestParam(name = "userID", required = true) long userID,
			@RequestParam(name = "pdf", required = true) InputStreamResource pdf) {
		// If the user doesn't exist, respond false
		if (!ResumeUserDBCHandle.getUserDBC().getUserValid(user, userID))
			return ResponseEntity.badRequest().build();

		// Store
		Long l = PdfDBCHandle.getPdfDBC().addPdf(user, pdf);

		return getOkResponse().body(l);
	}

	@DeleteMapping(name = "/pdf")
	public ResponseEntity<Void> deletePdf(@RequestParam(name = "user", required = true) String user,
			@RequestParam(name = "userID", required = true) long userID,
			@RequestParam(name = "pdfID", required = true) long pdfID) {
		// If the user doesn't exist, respond false
		ResumeUserDBC dbc = ResumeUserDBCHandle.getUserDBC();
		if (!dbc.getUserValid(user, userID))
			return ResponseEntity.badRequest().build();

		// Remove if possible
		var result = this.dbc.deletePdfFromDB(user, pdfID);
		if (result == DeleteResult.SUCCESS)
			return getOkResponse().build();
		else
			return ResponseEntity.badRequest().build();
	}
}
