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

public class RestRequest {

    private HashMap<String, List<String>> headers = new HashMap<>();
    private HttpMethod method;
    private HttpRequestBase baseRequest;
    private String url = null;
    private String body;

    public RestRequest(HttpMethod method) {
        this.method = method;
    }

    public RestRequest setEndpoint(Endpoint endpoint) {
        try {
            // Do the magic to convert all stuff to UTF-8
            URL urlObj = new URL(endpoint.getAddress());
            URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(),
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

    public RestRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public RestRequest setBody(JSONObject object) {
        this.body = object.toString();
        return this;
    }

    public RestRequest setBody(JSONArray object) {
        this.body = object.toString();
        return this;
    }

    public RestRequest addHeader(String name, String value) {
        List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
        headerList.add(value);
        return this;
    }

    public RestRequest addHeaders(String name, List<String> values) {
        List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
        headerList.addAll(values);
        return this;
    }

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

    private void fillHeaders(HttpRequestBase request) {
        // Add the headers
        headers.entrySet().forEach(e -> {
            if (e.getValue() != null) {
                e.getValue().forEach(v -> request.addHeader(e.getKey(), v));
            }
        });
    }

    public HttpRequestBase getFinalRequest() {
        HttpRequestBase request = createRequest();
        fillHeaders(request);

        // Set the body
        if(method != HttpMethod.GET && body != null) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, "UTF-8"));
        }

        return request;
    }

    public HttpRequestBase getFinalRequestAsync() {
        HttpRequestBase request = createRequest();
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
