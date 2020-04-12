package to.us.resume_builder;

import to.us.resume_builder.spring.controller.PDFController;

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

public class ResumeExporter {
    private static final Logger LOG = Logger.getLogger(ResumeExporter.class.getName());

    public static String uploadPDF(Path pdf) throws IOException, InterruptedException, TimeoutException {
        ProcessBuilder builder = new ProcessBuilder("curl", "-F", "\"file=@" + pdf.toString() + "\"", "https://file.io");
        builder.directory(pdf.getParent().toFile());
        builder.redirectOutput(new File("./fileio.log"));
        builder.redirectError(new File("./fileio_error.log"));
        Process p = builder.start();
        if (!p.waitFor(ApplicationConfiguration.getInstance().getLong("export.timeout"), TimeUnit.SECONDS)) {
            LOG.warning("file.io upload timed out, check fileio_error.log");
            p.destroy();
            throw new TimeoutException();
        }
        return Files.readString(pdf);
    }

    public static boolean export(Path exportLocation, String latex) throws IOException {
        Path latexPath = Path.of(ApplicationConfiguration.getInstance().getString("export.tempLocation"), MiscUtils.randomAlphanumericString(16) + ".tex");
        if (!Files.exists(latexPath.getParent())) {
            // Generate the temp folder
            Files.createDirectory(latexPath.getParent());
        }
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
        return status;
    }

    private static boolean compileResumePDF(Path filePath) throws IOException {
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
}
