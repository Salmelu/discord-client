package cz.salmelu.discord.implementation.net.rest;

import cz.salmelu.discord.AsyncCallback;
import cz.salmelu.discord.DiscordRequestException;
import cz.salmelu.discord.RequestResponse;
import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.reflector.Serializer;
import cz.salmelu.discord.implementation.net.RateLimiter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Future;

public class DiscordRequester {

    private static final String LIB_URL = "salmelu.cz";
    private static final String LIB_VERSION = "0.0.1";
    private final String token;
    private final Serializer serializer;
    private final RateLimiter limiter;
    private final RestRequestSender sender;

    private boolean stopped = false;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public DiscordRequester(String token, Serializer serializer, RateLimiter limiter) {
        this.token = token;
        this.limiter = limiter;
        this.serializer = serializer;
        this.sender = new RestRequestSender();
    }

    private void waitForLimit(Endpoint endpoint) {
        long waitFor = limiter.checkLimit(endpoint);
        while(waitFor != 0) {
            logger.info("Request to endpoint " + endpoint + " is being rate limited, " +
                    "waiting for " + waitFor + " milliseconds.");
            try {
                Thread.sleep(waitFor);
            }
            catch (InterruptedException ignored) {}
            waitFor = limiter.checkLimit(endpoint);
        }
    }

    void updateLimit(Endpoint endpoint, RestResponse response) {
        if (response.getStatusCode() == 429) {
            // We've reached a limit, eww
            final JSONObject retryResponse = new JSONObject(response.getResponseBody());
            final boolean isGlobal = retryResponse.getBoolean("global");
            final long retryAfter = retryResponse.getLong("retry_after");
            if(isGlobal) {
                limiter.globalLimitExceeded(retryAfter);
            }
            else {
                limiter.updateLimitRetry(endpoint, retryAfter);
            }
            return;
        }
        // Check if we have a header with remaining limit
        if(response.hasHeader("X-RateLimit-Limit")) {
            final int remaining = Integer.parseInt(response.getFirstHeader("X-RateLimit-Remaining"));
            final long reset = Long.parseLong(response.getFirstHeader("X-RateLimit-Reset"));
            limiter.updateLimit(endpoint, reset, remaining);
        }
    }

    private void throwRequestException(RestResponse response) {
        final int status = response.getStatusCode();
        switch(status) {
            case 200:
            case 201:
            case 204:
                // Request completed just fine, skip this
                break;
            case 304:
                // Not modified, let's warn user something changed and the request was therefore invalid
                if(response.getFirstHeader("Content-Type").equals("application/json")) {
                    JSONObject object = new JSONObject(response.getResponseBody());
                    logger.warn(object.getString("message"));
                    throw new DiscordRequestException(object.getString("message"), 304);
                }
                throw new DiscordRequestException("Unknown error has happened.", 304);
            default:
                // error, report it
                logger.error("HTTP request returned error code " + response.getStatusCode()
                        + " (" + response.getStatusText() + ")");
                throw new DiscordRequestException(response.getStatusText(), response.getStatusCode());
        }
    }

    public void stop() {
        this.stopped = true;
        try {
            sender.shutdown();
        }
        catch (IOException e) {
            logger.warn("Shutdown was unsuccessful.", e);
        }
    }

    private void fillHeaders(RestRequest request) {
        request.addHeader("User-Agent", "DiscordBot (" + LIB_URL + ", " + LIB_VERSION + ")")
                .addHeader("Authorization", "Bot " + token)
                .addHeader("Content-Type", "application/json");
    }

    private String fireRequest(Endpoint endpoint, RestRequest request) {
        RestResponse response = null;
        try {
            response = sender.sendRequest(request);
            throwRequestException(response);
        }
        finally {
            updateLimit(endpoint, response);
        }
        return response.getResponseBody();
    }

    private Future<RequestResponse> fireRequestAsync(Endpoint endpoint, RestRequest request, AsyncCallback callback) {
        return sender.sendAsyncRequest(request, endpoint, this, callback);
    }

    private String getRequestImpl(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending GET request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.GET)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    private String postRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending POST request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.POST)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    private Future<RequestResponse> postRequestImplAsync(Endpoint endpoint, String s, AsyncCallback callback) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous POST request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.POST)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request, callback);
    }

    private String patchRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending PATCH request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.PATCH)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    private Future<RequestResponse> patchRequestImplAsync(Endpoint endpoint, String s, AsyncCallback callback) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous PATCH request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.PATCH)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request, callback);
    }

    private String putRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending PUT request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.PUT)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    private Future<RequestResponse> putRequestImplAsync(Endpoint endpoint, String s, AsyncCallback callback) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous PUT request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.PUT)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request, callback);
    }

    private String deleteRequestImpl(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending DELETE request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.DELETE)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    private Future<RequestResponse> deleteRequestImplAsync(Endpoint endpoint, AsyncCallback callback) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous DELETE request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.DELETE)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request, callback);
    }

    public void getRequest(Endpoint endpoint) {
        getRequestImpl(endpoint);
    }

    public JSONObject getRequestAsObject(Endpoint endpoint) {
        String response = getRequestImpl(endpoint);
        return response == null ? null : new JSONObject(response);
    }

    public JSONArray getRequestAsArray(Endpoint endpoint) {
        String response = getRequestImpl(endpoint);
        return response == null ? null : new JSONArray(response);
    }

    public void postRequest(Endpoint endpoint) {
        postRequestImpl(endpoint, null);
    }

    public void postRequest(Endpoint endpoint, JSONObject object) {
        postRequestImpl(endpoint, object.toString());
    }

    public void postRequest(Endpoint endpoint, MappedObject object) {
        postRequest(endpoint, serializer.serialize(object));
    }

    public JSONObject postRequestAsObject(Endpoint endpoint) {
        String response = postRequestImpl(endpoint, null);
        return response == null ? null : new JSONObject(response);
    }

    public JSONObject postRequestAsObject(Endpoint endpoint, JSONObject object) {
        String response = postRequestImpl(endpoint,  object.toString());
        return response == null ? null : new JSONObject(response);
    }

    public JSONObject postRequestAsObject(Endpoint endpoint, MappedObject object) {
        return postRequestAsObject(endpoint, serializer.serialize(object));
    }

    public Future<RequestResponse> postRequestAsync(Endpoint endpoint, AsyncCallback callback) {
        return postRequestImplAsync(endpoint, null, callback);
    }

    public Future<RequestResponse> postRequestAsync(Endpoint endpoint, JSONObject object, AsyncCallback callback) {
        return postRequestImplAsync(endpoint, object.toString(), callback);
    }

    public Future<RequestResponse> postRequestAsync(Endpoint endpoint, MappedObject object, AsyncCallback callback) {
        return postRequestAsync(endpoint, serializer.serialize(object), callback);
    }

    public void patchRequest(Endpoint endpoint, JSONObject object) {
        patchRequestImpl(endpoint, object.toString());
    }

    public void patchRequest(Endpoint endpoint, MappedObject object) {
        patchRequest(endpoint, serializer.serialize(object));
    }

    public Future<RequestResponse> patchRequestAsync(Endpoint endpoint, JSONObject object, AsyncCallback callback) {
        return patchRequestImplAsync(endpoint, object.toString(), callback);
    }

    public Future<RequestResponse> patchRequestAsync(Endpoint endpoint, MappedObject object, AsyncCallback callback) {
        return patchRequestAsync(endpoint, serializer.serialize(object), callback);
    }

    public void putRequest(Endpoint endpoint) {
        putRequestImpl(endpoint, null);
    }

    public void putRequest(Endpoint endpoint, JSONObject object) {
        putRequestImpl(endpoint, object.toString());
    }

    public void putRequest(Endpoint endpoint, MappedObject object) {
        putRequest(endpoint, serializer.serialize(object));
    }

    public Future<RequestResponse> putRequestAsync(Endpoint endpoint, AsyncCallback callback) {
        return putRequestImplAsync(endpoint, null, callback);
    }

    public Future<RequestResponse> putRequestAsync(Endpoint endpoint, JSONObject object, AsyncCallback callback) {
        return putRequestImplAsync(endpoint, object.toString(), callback);
    }

    public Future<RequestResponse> putRequestAsync(Endpoint endpoint, MappedObject object, AsyncCallback callback) {
        return putRequestAsync(endpoint, serializer.serialize(object), callback);
    }

    public void deleteRequest(Endpoint endpoint) {
        deleteRequestImpl(endpoint);
    }

    public Future<RequestResponse> deleteRequestAsync(Endpoint endpoint, AsyncCallback callback) {
        return deleteRequestImplAsync(endpoint, callback);
    }

}
