package to.us.resume_builder.request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostRequest extends BasicRequest<InputStream> {

    private static final Logger LOG = Logger.getLogger(PostRequest.class.getName());

    private String resourceName;
    private static final String BOUNDARY = "----BOUNDARY_MARKER";

    public PostRequest(String specificPath) {
        this(specificPath, null);
    }

    public PostRequest(String specificPath, String rssName) {
        super(RequestType.POST, specificPath);
        resourceName = rssName;
    }

    @Override
    public URI getURI(String... arguments) {
        String s = SITE + path + PARAM_SPLIT + getArguments(arguments);
        return URI.create(s);
    }

    @Override
    protected BodyPublisher getBody(String... arguments) {
        BodyPublisher result = BodyPublishers.noBody();
        Path p = Path.of(resourceName);

        // Get file name
        String[] splitPath = resourceName.replace('\\', '/').split("/");
        String filename = splitPath[splitPath.length - 1];

        try {
            // Get temp file
            File tmp = File.createTempFile(filename, ".pdf");
            tmp.deleteOnExit();
            result = multipartFormFile(p, tmp, filename);
        } catch (IOException e) {
            LOG.logp(Level.SEVERE, PostRequest.class.getName(), "getBody",
                    "Exception thrown: " + e.getLocalizedMessage(), e);
        }

        return result;
    }

    @Override
    protected BodyHandler<InputStream> getResponseBuilder() {
        return HttpResponse.BodyHandlers.ofInputStream();
    }

    @Override
    protected String getType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    private BodyPublisher multipartFormFile(Path p, File tmp, String filename) {
        BodyPublisher result = BodyPublishers.noBody();
        final String boundary = "--" + BOUNDARY;
        final String ln = "\r\n";

        // Use temporary file
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(tmp))) {
            StringBuilder bldr = new StringBuilder();

            // Get a UUID
            UUID qquuid = UUID.randomUUID();
            bldr.append(boundary).append("Content-Disposition:" + ln + "form-data; name=\"qquuid\"").append(ln + ln)
                    .append(qquuid.toString()).append(ln);

            // Write file size
            bldr.append(boundary).append(ln).append("Content-Disposition: form-data; name=\"qqtotalfilesize\"")
                    .append(ln + ln).append(Files.size(p)).append(ln);

            // Write file data
            bldr.append(boundary).append(ln).append("Content-Disposition: form-data; name=file; filename=\"" + filename
                    + "\"" + ln + "Content-Type:application/x-object" + ln + ln);

            String res = bldr.toString();
            writer.write(res.getBytes(StandardCharsets.UTF_8));
            Files.copy(p, writer);
            writer.write((ln + boundary + "--").getBytes(StandardCharsets.UTF_8));
        } catch (IOException | InvalidPathException ex) {
            LOG.logp(Level.SEVERE, PostRequest.class.getName(), "getBody",
                    "Exception thrown: " + ex.getLocalizedMessage(), ex);
        }

        try {
            result = BodyPublishers.ofFile(tmp.toPath());
        } catch (IOException ex) {
            LOG.logp(Level.SEVERE, PostRequest.class.getName(), "multipartFormFile",
                    "Exception thrown: " + ex.getLocalizedMessage(), ex);
        }

        return result;
    }
}
