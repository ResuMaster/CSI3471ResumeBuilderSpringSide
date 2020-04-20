package to.us.resume_builder.spring.controller;

import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import to.us.resume_builder.spring.service.PDFService;

/**
 * PDFController contains the /pdf endpoint allowing a caller to give a LaTeX
 * string and compile it to PDF and have it returned directly or via file.io
 *
 * @author Jacob Curtis
 * @author Micah Schiewe
 */
@RestController
public class PDFController extends BasicController {
	private static final Logger LOG = Logger.getLogger(PDFController.class.getName());

	private PDFService service;

	public PDFController() {
		service = new PDFService();
	}

	/**
	 * This endpoint takes a latex string and a url boolean specifying if the caller
	 * wants the compiled PDF file to be uploaded to file.io instead of being
	 * directly returned in the response.
	 *
	 * @param latex The LaTeX string to be compiled to PDF.
	 * @param url   Whether the caller wants the PDF uploaded to file.io
	 * @return The response the user requested, or possibly an error.
	 */
	@PostMapping("/pdf")
	public ResponseEntity<InputStreamResource> postPdf(@RequestParam(name = "latex") String latex,
			@RequestParam(name = "url") boolean url) {
		return service.handleRequest(latex, url);
	}
}
