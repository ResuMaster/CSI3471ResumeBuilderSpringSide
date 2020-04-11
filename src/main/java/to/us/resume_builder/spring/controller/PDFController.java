package to.us.resume_builder.spring.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

	@PostMapping(name = "/pdf/url", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InputStreamResource> postPdfUrl(@RequestParam(name = "latex") String latex) {
		Path pdf;
		InputStreamResource rsc;
		try {
			pdf = export(latex);
			String response = uploadPDF(pdf);
			rsc = new InputStreamResource(new ByteArrayInputStream(response.getBytes()));
		} catch (IOException | InterruptedException e) {
			return ResponseEntity.status(500).build();
		}
		return getOkResponse().contentType(MediaType.APPLICATION_JSON).body(rsc);
	}

	@PostMapping(name = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> postPdf(@RequestParam(name = "latex") String latex) {
		Path pdf;
		InputStreamResource rsc;
		try {
			pdf = export(latex);
			rsc = new InputStreamResource(new BufferedInputStream(new FileInputStream(String.valueOf(pdf))));
		} catch (IOException e) {
			return ResponseEntity.status(500).build();
		}
		return getOkResponse().contentType(MediaType.APPLICATION_PDF).body(rsc);
	}

	private String uploadPDF(Path pdf) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder("curl", "-F", "\"file=@" + pdf.toString() + "\"", "https://file.io");
		builder.directory(pdf.getParent().toFile());
		builder.redirectOutput(new File("./fileio.log"));
		builder.redirectError(new File("./fileio_error.log"));
		Process p = builder.start();
		/*ApplicationConfiguration.getInstance().getLong("export.timeout")*/
		if (!p.waitFor(60L, TimeUnit.SECONDS)) {
			p.destroy();
		}
		return Files.readString(pdf);
	}

	private Path export(String latex) throws IOException {
		Path latexPath = Path.of("./", randomAlphanumericString(16) + ".tex");
		Path exportLocation = Path.of("./", "resume.pdf");
		if (!Files.exists(latexPath.getParent())) {
			// Generate the temp folder
			Files.createDirectory(latexPath.getParent());
			// Generate the LaTeX file
			Files.writeString(latexPath, latex, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
			// Generate the PDF
			boolean status = compileResumePDF(latexPath);
			// Save the pdf to the specified location
			if (status) {
				Files.move(latexPath.resolveSibling(latexPath.getFileName().toString().split("\\.")[0] + ".pdf"), exportLocation, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.deleteIfExists(latexPath.resolveSibling(latexPath.getFileName().toString().split("\\.")[0] + ".pdf"));
			}
		}
		return exportLocation;
	}

	private boolean compileResumePDF(Path filePath) throws IOException {
		// Temporary artifacts
		final String[] ARTIFACTS_TO_DELETE = { "aux", "log", "tex" };
		boolean status = true;

		// Attempt to generate the resume
		try {
			ProcessBuilder builder = new ProcessBuilder("pdflatex", "\"" + filePath.toAbsolutePath().toString() + "\"");
			builder.directory(filePath.getParent().toFile());
			// TODO: add dedicated log file
			builder.redirectOutput(new File("./export.log"));
			builder.redirectError(new File("./export.log"));

			// Run the process
			Process p = builder.start();
			/*ApplicationConfiguration.getInstance().getLong("export.timeout")*/
			if (!p.waitFor(60L, TimeUnit.SECONDS)) {
				p.destroy();
				status = false;
			}

			// Clean up artifacts
			for (File f : Objects.requireNonNull(filePath.getParent().toFile().listFiles())) {
				if (f.isFile() && Arrays.stream(ARTIFACTS_TO_DELETE).anyMatch(e -> f.getName().endsWith(e))) {
					Files.deleteIfExists(f.toPath());
				}
			}
		} catch (InterruptedException e) {
			status = false;
		}

		return status;
	}

	private static String randomAlphanumericString(int length) {
		int leftLimit = 48;  // Numeral '0'
		int rightLimit = 122; // Letter 'z'
		Random random = new Random();

		return random.ints(leftLimit, rightLimit + 1)
			.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))  // filter out non-alphanumerics
			.limit(length)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}
}
