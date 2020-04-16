package to.us.resume_builder.dbc.request.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import to.us.resume_builder.dbc.request.BasicRequest;
import to.us.resume_builder.dbc.request.RequestType;

public class GetRequest extends BasicRequest<String> {

    public GetRequest(String path) {
        super(RequestType.GET, path);
    }

    @Override
    protected URI getURI(String... arguments) {
        StringBuilder sb = new StringBuilder(SITE);
        sb.append(path);
        sb.append(PARAM_SPLIT);
        sb.append(getArguments(arguments));
        return URI.create(sb.toString());
    }

    @Override
    protected BodyHandler<String> getResponseBuilder() {
        return HttpResponse.BodyHandlers.ofString();
    }

    @Override
    protected BodyPublisher getBody(String... arguments) {
        return HttpRequest.BodyPublishers.ofString("");
    }
}
