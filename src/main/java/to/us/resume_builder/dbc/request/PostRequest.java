package to.us.resume_builder.dbc.request;

import java.net.URI;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse.BodyHandler;

public class PostRequest extends BasicRequest {

    public PostRequest(String specificPath) {
        super(RequestType.POST, specificPath);
    }

    @Override
    protected URI getURI(String... arguments) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BodyPublisher getBody(String... arguments) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BodyHandler getResponseBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

}
