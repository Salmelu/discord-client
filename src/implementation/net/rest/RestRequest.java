package cz.salmelu.discord.implementation.net.rest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A single request to the Discord servers.
 */
public class RestRequest {

    private HashMap<String, List<String>> headers = new HashMap<>();
    private HttpMethod method;
    private String url = null;
    private String body;

    /**
     * Creates a new request using given HTTP method.
     * @param method used HTTP method
     */
    RestRequest(HttpMethod method) {
        this.method = method;
    }

    /**
     * Sets the endpoint of the request.
     * @param endpoint set endpoint
     * @return this
     */
    RestRequest setEndpoint(Endpoint endpoint) {
        try {
            // Do the magic to convert all stuff to UTF-8
            final URL urlObj = new URL(endpoint.getAddress());
            final URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(),
                    URLDecoder.decode(urlObj.getPath(), "UTF-8"), "", urlObj.getRef());
            url = uri.toURL().toString();
            if (urlObj.getQuery() != null && !urlObj.getQuery().trim().equals("")) {
                // Add the query part to the url
                if (!url.substring(url.length() - 1).equals("?")) {
                    url += "?";
                }
                url += urlObj.getQuery();
            }
            else if (url.substring(url.length() - 1).equals("?")) {
                // No query, remove trailing part
                url = url.substring(0, url.length() - 1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Sets the body of the request.
     * @param body request body
     * @return this
     */
    RestRequest setBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the body of the request.
     * @param object request body
     * @return this
     */
    RestRequest setBody(JSONObject object) {
        this.body = object.toString();
        return this;
    }

    /**
     * Sets the body of the request.
     * @param object request body
     * @return this
     */
    RestRequest setBody(JSONArray object) {
        this.body = object.toString();
        return this;
    }

    /**
     * Adds a HTTP header to the request.
     * @param name header name
     * @param value header value
     * @return this
     */
    RestRequest addHeader(String name, String value) {
        final List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
        headerList.add(value);
        return this;
    }

    /**
     * Adds multiple HTTP headers with the same name to the request.
     * @param name header name
     * @param values list of header values
     * @return this
     */
    RestRequest addHeaders(String name, List<String> values) {
        final List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
        headerList.addAll(values);
        return this;
    }

    /**
     * Creates base of the request for correct method.
     * @return bare request
     */
    private HttpRequestBase createRequest() {
        //Create the request
        HttpRequestBase request = null;
        switch(method) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                break;
            case PUT:
                request = new HttpPut(url);
                break;
            case PATCH:
                request = new HttpPatch(url);
                break;
            case DELETE:
                request = new HttpDelete(url);
                break;
        }
        return request;
    }

    /**
     * Fills in required headers.
     * @param request filled request
     */
    private void fillHeaders(HttpRequestBase request) {
        // Add the headers
        headers.entrySet().forEach(e -> {
            if (e.getValue() != null) {
                e.getValue().forEach(v -> request.addHeader(e.getKey(), v));
            }
        });
    }

    /**
     * Create a finalized request ready to be sent.
     * @return ready request
     */
    HttpRequestBase getFinalRequest() {
        final HttpRequestBase request = createRequest();
        fillHeaders(request);

        // Set the body
        if(method != HttpMethod.GET && body != null) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, "UTF-8"));
        }

        return request;
    }

    /**
     * Create a finalized asynchronous request ready to be sent.
     * @return ready asynchronous request
     */
    HttpRequestBase getFinalRequestAsync() {
        final HttpRequestBase request = createRequest();
        fillHeaders(request);

        if(method != HttpMethod.GET && body != null) {
            HttpEntity entity = new StringEntity(body, "UTF-8");
            // What if we miss content-type? Fix it
            if (request.getHeaders("content-type") == null || request.getHeaders("content-type").length == 0) {
                request.setHeader(entity.getContentType());
            }

            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                entity.writeTo(output);
                NByteArrayEntity byteEntity = new NByteArrayEntity(output.toByteArray());
                ((HttpEntityEnclosingRequestBase) request).setEntity(byteEntity);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return request;
    }
}
