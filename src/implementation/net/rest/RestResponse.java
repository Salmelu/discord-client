package cz.salmelu.discord.implementation.net.rest;

import com.mashape.unirest.http.utils.ResponseUtils;
import cz.salmelu.discord.DiscordRequestException;
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

/**
 * A response given by the Discord server to previously sent request.
 */
class RestResponse {

    private String responseBody;
    private final int statusCode;
    private final String statusText;
    private final HashMap<String, List<String>> headers = new HashMap<>();

    /**
     * Converts HTTP response into Rest response and extracts relevant data and request body.
     * @param response received response
     */
    RestResponse(HttpResponse response) {
        final HttpEntity entity = response.getEntity();

        // Save the headers
        for (Header header : response.getAllHeaders()) {
            final String name = header.getName();
            final List<String> headerList = headers.computeIfAbsent(name, k -> new ArrayList<>());
            headerList.add(header.getValue());
        }

        // Save status
        final StatusLine statusLine = response.getStatusLine();
        this.statusCode = statusLine.getStatusCode();
        this.statusText = statusLine.getReasonPhrase();

        // Process body if there was any
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

    /**
     * Gets parsed HTTP response body.
     * @return HTTP body
     */
    String getResponseBody() {
        return responseBody;
    }

    /**
     * Gets received HTTP status code.
     * @return HTTP status code
     */
    int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets received HTTP status text.
     * @return HTTP status text
     */
    String getStatusText() {
        return statusText;
    }

    /**
     * Gets the first header with the given name.
     * @param name header name
     * @return value if it exists, or null
     */
    String getFirstHeader(String name) {
        List<String> list = headers.get(name);
        return (list == null || list.size() < 1) ? null : list.get(0);
    }

    /**
     * Checks if the response contained a specific header.
     * @param name checked header name
     * @return true if it was present in the response
     */
    boolean hasHeader(String name) {
        List<String> list = headers.get(name);
        return (list != null && list.size() >= 1);
    }

    /**
     * Get all headers with the given name.
     * @param name header name
     * @return list of values or null, if the header is not present at all
     */
    List<String> getHeaders(String name) {
        return hasHeader(name) ? Collections.unmodifiableList(headers.get(name)) : null;
    }

    /**
     * Converts the response into {@link cz.salmelu.discord.RequestResponse},
     * which is presented to modules in the {@link java.util.concurrent.Future}.
     * @return matching {@link cz.salmelu.discord.RequestResponse}
     */
    RequestResponseImpl toRequestResponse() {
        return new RequestResponseImpl(statusCode, statusText);
    }
}
