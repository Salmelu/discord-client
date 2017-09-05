package cz.salmelu.discord.implementation.net.rest;

import com.mashape.unirest.http.utils.ResponseUtils;
import cz.salmelu.discord.DiscordRequestException;
import cz.salmelu.discord.RequestResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class RestResponse {

    private String responseBody;
    private final int statusCode;
    private final String statusText;
    private final HashMap<String, List<String>> headers = new HashMap<>();

    public RestResponse(HttpResponse response) {
        final HttpEntity entity = response.getEntity();

        for (Header header : response.getAllHeaders()) {
            final String name = header.getName();
            final List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
            headerList.add(header.getValue());
        }

        final StatusLine statusLine = response.getStatusLine();
        this.statusCode = statusLine.getStatusCode();
        this.statusText = statusLine.getReasonPhrase();

        if(entity != null) {
            String charset = "UTF-8";

            Header contentType = entity.getContentType();
            if (contentType != null) {
                String responseCharset = ResponseUtils.getCharsetFromContentType(contentType.getValue());
                if (responseCharset != null && !responseCharset.trim().equals("")) {
                    charset = responseCharset;
                }
            }

            try {
                byte[] rawBody;
                InputStream responseInputStream = entity.getContent();
                if (ResponseUtils.isGzipped(entity.getContentEncoding())) {
                    responseInputStream = new GZIPInputStream(entity.getContent());
                }
                rawBody = ResponseUtils.getBytes(responseInputStream);
                responseBody = new String(rawBody, charset);
            }
            catch (IOException e) {
                throw new DiscordRequestException("Couldn't process response from the server.", 400);
            }
        }
        else {
            responseBody = null;
        }
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getFirstHeader(String name) {
        List<String> list = headers.get(name);
        return (list == null || list.size() < 1) ? null : list.get(0);
    }

    public boolean hasHeader(String name) {
        List<String> list = headers.get(name);
        return (list != null && list.size() >= 1);
    }

    public List<String> getHeaders(String name) {
        return Collections.unmodifiableList(headers.get(name));
    }

    public RequestResponseImpl toRequestResponse() {
        return new RequestResponseImpl(statusCode, statusText, headers);
    }
}
