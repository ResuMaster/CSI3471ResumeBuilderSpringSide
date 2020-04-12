package to.us.resume_builder.spring.controller;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import to.us.resume_builder.ResumeExporter;

@RestController
public class PDFController extends BasicController {
	private static final Logger LOG = Logger.getLogger(PDFController.class.getName());

	@PostMapping("/pdf")
	public ResponseEntity<InputStreamResource> postPdfUrl(@RequestParam(name = "latex") String latex, @RequestParam(name = "url") boolean url) {
		LOG.info(latex);
		Path pdf = Path.of("./", "resume.pdf");
		InputStreamResource rsc;
		MediaType mediaType;
		try {
			if (!ResumeExporter.export(pdf, latex)) {
				LOG.warning("Export failed");
				return ResponseEntity.status(500).build();
			}
			LOG.info("requesting url: " + url);
			if (url) {
				LOG.info("Uploading pdf to file.io");
				String response = ResumeExporter.uploadPDF(pdf);
				rsc = new InputStreamResource(new ByteArrayInputStream(response.getBytes()));
				mediaType = MediaType.APPLICATION_JSON;
			} else {
				rsc = new InputStreamResource(new BufferedInputStream(new FileInputStream(String.valueOf(pdf))));
				mediaType = MediaType.APPLICATION_PDF;
			}
		} catch (FileNotFoundException e) {
			LOG.warning("could not find " + pdf.toString() + " - " + e.getMessage());
			return getInternalServerErrorResponse("internal PDF file not found").build();
		} catch (InterruptedException | TimeoutException e) {
			LOG.warning("file.io upload failed - " + e.getMessage());
			return getInternalServerErrorResponse("file.io upload failed").build();
		} catch (IOException e) {
			LOG.warning("export failure -> " + e.getMessage());
			return getInternalServerErrorResponse("export failed").build();
		}
		return getOkResponse().contentType(mediaType).body(rsc);
	}
}
