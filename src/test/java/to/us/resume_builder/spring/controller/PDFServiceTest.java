package to.us.resume_builder.spring.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import to.us.resume_builder.MiscUtils;
import to.us.resume_builder.ResumeExporter;
import to.us.resume_builder.spring.service.PDFService;

class PDFServiceTest {

    @Mocked
    private ResumeExporter bldr;

    private static PDFService pc;
    private static final String FILE_IO = "Awesome file.io response";

    private static final String FILE_NAME = "aCoolFile";
    private static final byte[] PDF = {
            'P', 'D', 'F'
    };

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        pc = new PDFService();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        pc = null;
    }

    // #############################################################################
    // ############################### TEST SUCCESS ################################
    // #############################################################################

    /**
     * Tests that requesting uploading a PDF to file.io succeeds, and sends back the
     * site's response (if it connects).
     * 
     * @throws IOException           If an error occurs when reading the response.
     * @throws IllegalStateException Thrown by getInputStream for an undefined
     *                               reason. Seriously, guys, finish your Javadoc
     *                               comments! They're incredibly useful!
     */
    @Test
    void testExportSuccessToURL() throws IOException, IllegalStateException {
        try {
            new Expectations() {
                {
                    // Export to PDF succeeds
                    ResumeExporter.export((Path) any, anyString);
                    result = true;

                    // Upload to file.io happens w/o a hitch
                    ResumeExporter.uploadPDF((Path) any);
                    result = FILE_IO;
                }
            };
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            fail("Exception thrown in mock initialization.");
        }

        // Ensure response OK
        ResponseEntity<InputStreamResource> rss = pc.handleRequest("", true);
        assertTrue(rss.getStatusCode().is2xxSuccessful());
        assertTrue(Arrays.equals(rss.getBody().getInputStream().readAllBytes(), FILE_IO.getBytes()));
    }

    /**
     * Test that the service will seek to export to the file and return the
     * information correctly.
     * 
     * @param util Mock of MiscUtils, allowing randomAlphanumericString to be
     *             replaced with a predefined filename
     * @throws IOException           If an error occurs when reading the response.
     * @throws IllegalStateException Thrown by getInputStream for an undefined
     *                               reason. I won't beat a dead horse here.
     */
    @Test
    void testExportSuccessToFile(@Mocked MiscUtils util) throws IOException, IllegalStateException {
        // Prepare file
        File f = new File("./" + FILE_NAME + ".pdf");

        try {
            // Fake export to create the test data file
            new MockUp<ResumeExporter>() {
                @Mock
                boolean export(Path exportLocation, String latex) throws IOException {
                    f.createNewFile();
                    BufferedWriter br = Files.newBufferedWriter(f.toPath());
                    for (byte b : PDF)
                        br.write(b);
                    br.close();
                    return true;
                }
            };

            // Mock filename to give same one each time
            new Expectations() {
                {
                    // Give a predefined filename
                    MiscUtils.randomAlphanumericString(anyInt);
                    result = FILE_NAME;
                }
            };

            // Ensure response OK
            ResponseEntity<InputStreamResource> rss = pc.handleRequest("", false);
            assertTrue(rss.getStatusCode().is2xxSuccessful());
            assertTrue(Arrays.equals(rss.getBody().getInputStream().readAllBytes(), PDF));
        } finally {
            // Clear file if created
            if (f.exists())
                f.delete();
        }
    }

    // #############################################################################
    // ############################### TEST FAILURE ################################
    // #############################################################################

    /**
     * Ensure the program responds correctly to a failed export
     */
    @Test
    void testExportFail() {
        try {
            new Expectations() {
                {
                    ResumeExporter.export((Path) any, anyString);
                    result = false;
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception thrown in mock initialization.");
        }

        ResponseEntity<InputStreamResource> rss = pc.handleRequest("", true);
        assertTrue(rss.getStatusCode().is4xxClientError());
    }

    /**
     * Tests a variety of possible failures from the ResumeExporter, ensuring the
     * service handles them correctly.
     * 
     * @throws IOException           If an error occurs when reading the response.
     * @throws IllegalStateException Potentially thrown by getInputStream.
     */
    @ParameterizedTest(name = "{index} => ex={0}, test={1}")
    @MethodSource("resumeExporterExceptions")
    void testResumeExporterFailure(Exception ex, CodeTest test) throws IOException, IllegalStateException {
        // Mock the export method with a throw
        new Expectations() {
            {
                ResumeExporter.export((Path) any, anyString);
                result = ex;
            }
        };

        // Test for the expected error code returned to the caller from this throw
        ResponseEntity<InputStreamResource> rss = pc.handleRequest("", false);
        assertTrue(test.validCode(rss.getStatusCode()));
    }

    /**
     * Provides the exceptions which ResumeExporter may throw, as well as matchers
     * to ensure they're the correct error.
     * 
     * @return The exceptions and
     */
    private static Stream<Arguments> resumeExporterExceptions() {
        CodeTest server = e -> e.is5xxServerError();
        CodeTest client = e -> e.is4xxClientError();

        return Stream.of(Arguments.of(new InterruptedException(), server), Arguments.of(new IOException(), server),
                Arguments.of(new TimeoutException(), client));
    }

    @FunctionalInterface
    private interface CodeTest {
        boolean validCode(HttpStatus status);
    }
}
