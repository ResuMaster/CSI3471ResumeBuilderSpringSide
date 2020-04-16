package to.us.resume_builder.spring.controller;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import to.us.resume_builder.MiscUtils;
import to.us.resume_builder.ResumeExporter;

/**
 * PDFController contains the /pdf endpoint allowing a caller to give a
 * LaTeX string and compile it to PDF and have it returned directly or via
 * file.io
 *
 * @author Jacob Curtis
 * @author Micah Schiewe
 */
@RestController
public class PDFController extends BasicController {
	private static final Logger LOG = Logger.getLogger(PDFController.class.getName());

	/**
	 * This endpoint takes a latex string and a url boolean specifying if the
	 * caller wants the compiled PDF file to be uploaded to file.io instead of
	 * being directly returned in the response.
	 *
	 * @param latex The LaTeX string to be compiled to PDF.
	 * @param url Whether the caller wants the PDF uploaded to file.io
	 * @return The response the user requested, or possibly an error.
	 */
	@PostMapping("/pdf")
	public ResponseEntity<InputStreamResource> postPdf(@RequestParam(name = "latex") String latex,
													   @RequestParam(name = "url") boolean url) {
		Path pdf;
		do {
			pdf = Path.of("./", MiscUtils.randomAlphanumericString(16) + ".pdf");
		} while (Files.exists(pdf));

		InputStreamResource rsc;
		MediaType mediaType;
		InputStream in;
		try {
			if (!ResumeExporter.export(pdf, latex)) {
				LOG.warning("PDF compilation failed");
				return badRequest("Could not convert LaTeX to PDF").build();
			}

			LOG.info("requesting url: " + url);
			if (url) {
				// Upload PDF to file.io, return response
				LOG.info("Uploading pdf to file.io");
				String response = ResumeExporter.uploadPDF(pdf);
				in = new ByteArrayInputStream(response.getBytes());
				mediaType = MediaType.APPLICATION_JSON;
			} else {
				// Give the PDF in the response
				in = new ByteArrayInputStream(Files.readAllBytes(pdf));
				mediaType = MediaType.APPLICATION_PDF;
			}
		} catch (FileNotFoundException e) {
			LOG.warning("could not find " + pdf.toString() + " - " + e.getMessage());
			return internalServerError("internal file not found").build();
		} catch (InterruptedException e) {
			LOG.warning("file.io upload failed - " + e.getMessage());
			return serviceUnavailable("file.io upload failed").build();
		} catch (TimeoutException e) {
			LOG.warning("file.io upload timed out - " + e.getMessage());
			return requestTimeout("file.io upload timed out").build();
		} catch (IOException e) {
			LOG.logp(Level.WARNING, getClass().getName(), "postPdf", "IOException", e);
			return internalServerError("internal file I/O error").build();
		}

		// Clean up PDF file
		try {
			Files.deleteIfExists(pdf.toAbsolutePath());
		} catch (IOException e) {
			LOG.warning("could not delete " + pdf.toString() + " - " + e);
		}

		rsc = new InputStreamResource(new BufferedInputStream(in));
		return ok().contentType(mediaType).body(rsc);
	}
}
