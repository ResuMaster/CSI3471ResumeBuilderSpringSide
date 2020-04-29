package to.us.resume_builder.spring.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import to.us.resume_builder.MiscUtils;
import to.us.resume_builder.ResumeExporter;

public class PDFService {
    private static final ResponseFactory FACTORY = ResponseFactory.getFactory();
    private Logger LOG = Logger.getLogger(PDFService.class.getName());

    public ResponseEntity<InputStreamResource> handleRequest(String latex, boolean url) {
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
                return FACTORY.createBadRequest("Could not convert LaTeX to PDF").build();
            }

            LOG.info("requesting url: " + url);
            if (url) {
                // Upload PDF to file.io, return response
                LOG.info("Uploading pdf to transfer.sh");
                String response = ResumeExporter.uploadPDF(pdf);
                in = new ByteArrayInputStream(response.getBytes());
                mediaType = MediaType.APPLICATION_JSON;
            } else {
                // Give the PDF in the response
                in = new ByteArrayInputStream(Files.readAllBytes(pdf));
                mediaType = MediaType.APPLICATION_PDF;
            }
        } catch (InterruptedException e) {
            LOG.warning("transfer.sh upload failed - " + e.getMessage());
            return FACTORY.createServiceUnavailable("file.io upload failed").build();
        } catch (TimeoutException e) {
            LOG.warning("transfer.sh upload timed out - " + e.getMessage());
            return FACTORY.createRequestTimeout("file.io upload timed out").build();
        } catch (IOException e) {
            LOG.logp(Level.WARNING, getClass().getName(), "postPdf", "IOException", e);
            return FACTORY.createInternalServerError("internal file I/O error").build();
        }

        // Clean up PDF file
        try {
            Files.deleteIfExists(pdf.toAbsolutePath());
        } catch (IOException e) {
            LOG.warning("could not delete " + pdf.toString() + " - " + e);
        }

        rsc = new InputStreamResource(new BufferedInputStream(in));
        return FACTORY.createOk().contentType(mediaType).body(rsc);
    }
}
