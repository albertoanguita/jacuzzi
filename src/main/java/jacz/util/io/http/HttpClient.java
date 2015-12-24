package jacz.util.io.http;

import jacz.util.lists.tuple.Duple;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simple Http Client based on Apache implementation
 */
public class HttpClient {

    public enum Verb {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS,
        TRACE
    }

    public enum ContentType {
        PLAIN,
        HTML,
        CSV,
        YAML,
        XML,
        JSON
    }

    public static Duple<Integer, String> httpRequest(String url, Verb verb, ContentType acceptType) throws IOException {
        return httpRequest(url, verb, acceptType, null, null);
    }

    public static Duple<Integer, String> httpRequest(String url, Verb verb, ContentType acceptType, ContentType contentType, String content) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpRequestBase request = getRequest(url, verb);
        request.addHeader("accept", getContentType(acceptType));

        if (contentType != null) {
            StringEntity input = new StringEntity(content);
            input.setContentType(getContentType(contentType));
            ((HttpEntityEnclosingRequestBase) request).setEntity(input);
        }

        HttpResponse response = client.execute(request);

        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));
        StringBuilder responseContent = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            responseContent.append(line);
            responseContent.append('\n');
        }
        client.close();
        return new Duple<>(response.getStatusLine().getStatusCode(), responseContent.toString());
    }

    private static HttpRequestBase getRequest(String url, Verb verb) {
        switch (verb) {

            case GET:
                return new HttpGet(url);

            case POST:
                return new HttpPost(url);

            case PUT:
                return new HttpPut(url);

            case DELETE:
                return new HttpDelete(url);

            case PATCH:
                return new HttpPatch(url);

            case HEAD:
                return new HttpHead(url);

            case OPTIONS:
                return new HttpOptions(url);

            case TRACE:
                return new HttpTrace(url);

            default:
                throw new IllegalArgumentException();
        }
    }

    private static String getContentType(ContentType contentType) {
        switch (contentType) {

            case PLAIN:
                return "text/plain";

            case HTML:
                return "text/html";

            case CSV:
                return "text/csv";

            case YAML:
                return "text/yaml";

            case XML:
                return "application/xml";

            case JSON:
                return "application/json";

            default:
                throw new IllegalArgumentException();
        }
    }
}
