package to.us.resume_builder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Contains utility functions for exporting resume to PDF and uploading to
 * file.io
 *
 * @author Jacob Curtis
 * @author Matthew McCaskill
 */
public class ResumeExporter {
    private static final Logger LOG = Logger.getLogger(ResumeExporter.class.getName());

    /**
     * Uploads the specified PDF to file.io, and returns their response.
     *
     * @param pdf The PDF to upload to file.io
     *
     * @return The response from file.io
     * @throws IOException          Resulting from ProcessBuilder or Files
     * @throws InterruptedException The process' execution ended early
     * @throws TimeoutException     The process took too long
     */
    public static String uploadPDF(Path pdf) throws IOException, InterruptedException, TimeoutException {
        String name = pdf.toString().split("\\.pdf")[0];

        // Create the command
        ProcessBuilder builder = new ProcessBuilder("curl", "-F", "\"file=@" + pdf.toString() + "\"", "https://file.io");
        builder.directory(pdf.getParent().toFile());

        // Set up log files
        File out = new File("./" + name + ".log");
        File err = new File("./" + name + "_error.log");
//        builder.redirectOutput(out);
//        builder.redirectError(err);
        builder.inheritIO();

        // Run the command
        Process p = builder.start();
        if (!p.waitFor(ApplicationConfiguration.getInstance().getLong("export.timeout"), TimeUnit.SECONDS)) {
            LOG.warning("file.io upload timed out, check " + name + "_error.log");
            if (out.exists())
                System.out.println(">>> file.io process stdout: \n" + Files.readString(out.toPath()));
            if (err.exists())
                System.out.println(">>> file.io process stderr: \n" + Files.readString(err.toPath()));
            Files.deleteIfExists(out.toPath());
            p.destroy();
            throw new TimeoutException();
        }

        String response = Files.readString(out.toPath());
        if (out.exists())
            System.out.println(">>> file.io process stdout: \n" + response);
        if (err.exists())
            System.out.println(">>> file.io process stderr: \n" + Files.readString(err.toPath()));

        // Clean up logs
        Files.deleteIfExists(out.toPath());
        Files.deleteIfExists(err.toPath());

        return response;
    }

    /**
     * Export the resume to the specified file.
     *
     * @param exportLocation The name of the file to export to.
     *
     * @return Whether or not the export was successful.
     * @throws IOException Thrown if any errors occur during the export
     *                     process.
     */
    public static boolean export(Path exportLocation, String latex) throws IOException {
        Path latexPath = Path.of(ApplicationConfiguration.getInstance().getString("export.tempLocation"), MiscUtils.randomAlphanumericString(16) + ".tex");
        if (!Files.exists(latexPath.getParent())) {
            // Generate the temp folder
            Files.createDirectory(latexPath.getParent());
            LOG.info("Temporary folder did not exist, so it was created.");
        }
        // Generate the LaTeX file
        Files.writeString(latexPath, latex, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        LOG.info("LaTeX file created.");

        // Generate the PDF
        boolean status = compileResumePDF(latexPath);
        // Save the pdf to the specified location
        if (status && Files.exists(exportLocation)) {
            LOG.info("PDF compilation successful.");
            Path finalLocation = latexPath.resolveSibling(latexPath.getFileName().toString().split("\\.")[0] + ".pdf");
            LOG.info("Moving generated PDF to " + finalLocation.toAbsolutePath().toString() + "...");
            Files.move(finalLocation, exportLocation, StandardCopyOption.REPLACE_EXISTING);
        } else {
            LOG.warning("PDF compilation failed.");
            LOG.warning("Deleting temporary PDF, if it exists...");
            Files.deleteIfExists(latexPath.resolveSibling(latexPath.getFileName().toString().split("\\.")[0] + ".pdf"));
        }
        LOG.info("Export process complete.");
        return status;
    }

    /**
     * Compile the resume PDF from an existing <code>.tex</code> source.
     *
     * @param filePath The path to the <code>.tex</code> file to compile.
     *
     * @return Whether or not the compilation was successful.
     * @throws IOException Thrown if an I/O error occurs.
     */
    private static boolean compileResumePDF(Path filePath) throws IOException {
        LOG.info("Beginning resume compilation...");
        // Temporary artifacts
        final String[] ARTIFACTS_TO_DELETE = { "aux", "log", "tex", "log" };
        boolean status = true;

        String name = filePath.toString().split(".pdf")[0];
        File log = new File("./" + name + ".log");

        // Attempt to generate the resume
        try {
            ProcessBuilder builder = new ProcessBuilder("pdflatex", "\"" + filePath.toAbsolutePath().toString() + "\"");
            builder.directory(filePath.getParent().toFile());
            // TODO: add dedicated log file
//            builder.redirectOutput(log);
//            builder.redirectError(log);
            builder.inheritIO();

            LOG.info("PDF compilation log can be found at " + log.getAbsolutePath());

            // Run the process
            Process p = builder.start();
            LOG.info("Attempting to run process \"" + builder.command() + "\"...");
            if (!p.waitFor(ApplicationConfiguration.getInstance().getLong("export.timeout"), TimeUnit.SECONDS)) {
                p.destroy();
                LOG.warning("PDF compilation timed out.");
                status = false;
            }

            // Clean up artifacts
            for (File f : Objects.requireNonNull(filePath.getParent().toFile().listFiles())) {
                if (f.isFile() && Arrays.stream(ARTIFACTS_TO_DELETE).anyMatch(e -> f.getName().endsWith(e))) {
                    if (f.getName().endsWith("log")) {
                        // Don't delete log if something went wrong
                        LOG.info("Attempting to delete artifact \"" + f.getAbsolutePath() + "\", if it exists.");
                        if (status) {
                            Files.deleteIfExists(f.toPath());
                        }
                    } else {
                        LOG.info("Attempting to delete artifact \"" + f.getAbsolutePath() + "\", if it exists.");
                        Files.deleteIfExists(f.toPath());
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.warning("Compilation process was interrupted. Exiting compilation.");
            status = false;
        } finally {
            if (log.exists()) {
                System.out.println(">>> pdflatex process stdout/stderr: \n" + Files.readString(log.toPath()));
            }
        }

        return status;
    }
}
