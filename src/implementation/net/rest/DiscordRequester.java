package cz.salmelu.discord.implementation.net.rest;

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

/**
 * A requester for sending REST requests to Discord servers.
 */
public class DiscordRequester {

    private static final String LIB_URL = cz.salmelu.discord.DiscordModules.LIB_URL;
    private static final String LIB_VERSION = cz.salmelu.discord.DiscordModules.LIB_VERSION;
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

    /**
     * Blocks the thread until the request can be sent according to rate limits.
     * @param endpoint endpoint the request targets
     */
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

    /**
     * Updates a rate limit with a new value, depending on received response from the servers.
     * @param endpoint affected endpoint
     * @param response response from the servers
     */
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
        // Check if we have a header with remaining limit, if so, update it
        if(response.hasHeader("X-RateLimit-Limit")) {
            final int remaining = Integer.parseInt(response.getFirstHeader("X-RateLimit-Remaining"));
            final long reset = Long.parseLong(response.getFirstHeader("X-RateLimit-Reset"));
            limiter.updateLimit(endpoint, reset, remaining);
        }
    }

    /**
     * <p>Checks if there was a problem with the request and the response contained an error HTTP status code.</p>
     * <p>For error codes, this throws an exception describing the issue.</p>
     * @param response received response
     */
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
                    final JSONObject object = new JSONObject(response.getResponseBody());
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

    /**
     * Stops the requester and terminates all connections and threads.
     */
    public void stop() {
        this.stopped = true;
        try {
            sender.shutdown();
        }
        catch (IOException e) {
            logger.warn("Shutdown was unsuccessful.", e);
        }
    }

    /**
     * Adds the headers Discord always requires.
     * @param request filled request
     */
    private void fillHeaders(RestRequest request) {
        request.addHeader("User-Agent", "DiscordBot (" + LIB_URL + ", " + LIB_VERSION + ")")
                .addHeader("Authorization", "Bot " + token)
                .addHeader("Content-Type", "application/json");
    }

    /**
     * Sends the request and updates relevant structures (rate limits).
     * @param endpoint targeted endpoint
     * @param request sent request
     * @return reply to the response
     */
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

    /**
     * Sends the request asynchronously. The structures are updated in a callback after the request is completed.
     * @param endpoint targeted endpoint
     * @param request sent request
     * @return future to process the reply later
     */
    private Future<RequestResponse> fireRequestAsync(Endpoint endpoint, RestRequest request) {
        return sender.sendAsyncRequest(request, endpoint, this);
    }

    /**
     * Sends a GET request to given endpoint.
     * @param endpoint targeted endpoint
     * @return received reply
     */
    private String getRequestImpl(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending GET request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.GET)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    /**
     * Sends a POST request to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return received reply
     */
    private String postRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending POST request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.POST)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    /**
     * Sends a POST request asynchronously to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return future to collect the reply later
     */
    private Future<RequestResponse> postRequestImplAsync(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous POST request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.POST)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request);
    }

    /**
     * Sends a PATCH request to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return received reply
     */
    private String patchRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending PATCH request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.PATCH)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    /**
     * Sends a PATCH request asynchronously to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return future to collect the reply later
     */
    private Future<RequestResponse> patchRequestImplAsync(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous PATCH request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.PATCH)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request);
    }

    /**
     * Sends a PUT request to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return received reply
     */
    private String putRequestImpl(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending PUT request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.PUT)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    /**
     * Sends a PUT request asynchronously to given endpoint.
     * @param endpoint targeted endpoint
     * @param s sent data
     * @return future to collect the reply later
     */
    private Future<RequestResponse> putRequestImplAsync(Endpoint endpoint, String s) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous PUT request to " + endpoint.getAddress() + "; attachment: " + s);

        final RestRequest request = new RestRequest(HttpMethod.PUT)
                .setEndpoint(endpoint)
                .setBody(s);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request);
    }

    /**
     * Sends a DELETE request to given endpoint.
     * @param endpoint targeted endpoint
     * @return received reply
     */
    private String deleteRequestImpl(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending DELETE request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.DELETE)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequest(endpoint, request);
    }

    /**
     * Sends a DELETE request asynchronously to given endpoint.
     * @param endpoint targeted endpoint
     * @return future to collect the reply later
     */
    private Future<RequestResponse> deleteRequestImplAsync(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending asynchronous DELETE request to " + endpoint.getAddress());

        final RestRequest request = new RestRequest(HttpMethod.DELETE)
                .setEndpoint(endpoint);
        fillHeaders(request);
        return fireRequestAsync(endpoint, request);
    }

    /**
     * Sends a synchronous GET request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return response in a JSON object
     */
    public JSONObject getRequestAsObject(Endpoint endpoint) {
        String response = getRequestImpl(endpoint);
        return response == null ? null : new JSONObject(response);
    }

    /**
     * Sends a synchronous GET request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return response in a JSON array
     */
    public JSONArray getRequestAsArray(Endpoint endpoint) {
        String response = getRequestImpl(endpoint);
        return response == null ? null : new JSONArray(response);
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     */
    public void postRequest(Endpoint endpoint) {
        postRequestImpl(endpoint, null);
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void postRequest(Endpoint endpoint, JSONObject object) {
        postRequestImpl(endpoint, object.toString());
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void postRequest(Endpoint endpoint, MappedObject object) {
        postRequest(endpoint, serializer.serialize(object));
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return received reply
     */
    public JSONObject postRequestAsObject(Endpoint endpoint) {
        String response = postRequestImpl(endpoint, null);
        return response == null ? null : new JSONObject(response);
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return received reply
     */
    public JSONObject postRequestAsObject(Endpoint endpoint, JSONObject object) {
        String response = postRequestImpl(endpoint,  object.toString());
        return response == null ? null : new JSONObject(response);
    }

    /**
     * Sends a synchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return received reply
     */
    public JSONObject postRequestAsObject(Endpoint endpoint, MappedObject object) {
        return postRequestAsObject(endpoint, serializer.serialize(object));
    }

    /**
     * Sends an asynchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return future to check for potential problems
     */
    public Future<RequestResponse> postRequestAsync(Endpoint endpoint) {
        return postRequestImplAsync(endpoint, null);
    }

    /**
     * Sends an asynchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> postRequestAsync(Endpoint endpoint, JSONObject object) {
        return postRequestImplAsync(endpoint, object.toString());
    }

    /**
     * Sends an asynchronous POST request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> postRequestAsync(Endpoint endpoint, MappedObject object) {
        return postRequestAsync(endpoint, serializer.serialize(object));
    }

    /**
     * Sends a synchronous PATCH request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void patchRequest(Endpoint endpoint, JSONObject object) {
        patchRequestImpl(endpoint, object.toString());
    }

    /**
     * Sends a synchronous PATCH request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void patchRequest(Endpoint endpoint, MappedObject object) {
        patchRequest(endpoint, serializer.serialize(object));
    }

    /**
     * Sends an asynchronous PATCH request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> patchRequestAsync(Endpoint endpoint, JSONObject object) {
        return patchRequestImplAsync(endpoint, object.toString());
    }

    /**
     * Sends an asynchronous PATCH request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> patchRequestAsync(Endpoint endpoint, MappedObject object) {
        return patchRequestAsync(endpoint, serializer.serialize(object));
    }

    /**
     * Sends a synchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     */
    public void putRequest(Endpoint endpoint) {
        putRequestImpl(endpoint, null);
    }

    /**
     * Sends a synchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void putRequest(Endpoint endpoint, JSONObject object) {
        putRequestImpl(endpoint, object.toString());
    }

    /**
     * Sends a synchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     */
    public void putRequest(Endpoint endpoint, MappedObject object) {
        putRequest(endpoint, serializer.serialize(object));
    }

    /**
     * Sends an asynchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return future to check for potential problems
     */
    public Future<RequestResponse> putRequestAsync(Endpoint endpoint) {
        return putRequestImplAsync(endpoint, null);
    }

    /**
     * Sends an asynchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> putRequestAsync(Endpoint endpoint, JSONObject object) {
        return putRequestImplAsync(endpoint, object.toString());
    }

    /**
     * Sends an asynchronous PUT request to given endpoint.
     * @param endpoint targeted endpoint.
     * @param object attached object
     * @return future to check for potential problems
     */
    public Future<RequestResponse> putRequestAsync(Endpoint endpoint, MappedObject object) {
        return putRequestAsync(endpoint, serializer.serialize(object));
    }

    /**
     * Sends a synchronous DELETE request to given endpoint.
     * @param endpoint targeted endpoint.
     */
    public void deleteRequest(Endpoint endpoint) {
        deleteRequestImpl(endpoint);
    }

    /**
     * Sends an asynchronous DELETE request to given endpoint.
     * @param endpoint targeted endpoint.
     * @return future to check for potential problems
     */
    public Future<RequestResponse> deleteRequestAsync(Endpoint endpoint) {
        return deleteRequestImplAsync(endpoint);
    }
}
