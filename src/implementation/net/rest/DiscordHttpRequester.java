package cz.salmelu.discord.implementation.net.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.reflector.Serializer;
import cz.salmelu.discord.implementation.net.RateLimiter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordHttpRequester {

    private static final String LIB_URL = "salmelu.cz";
    private static final String LIB_VERSION = "0.0.1";
    private final String token;
    private final RateLimiter limiter;
    private final Serializer serializer;

    private boolean stopped = false;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public DiscordHttpRequester(String token, Serializer serializer, RateLimiter limiter) {
        this.token = token;
        this.limiter = limiter;
        this.serializer = serializer;
    }

    public void stop() {
        this.stopped = true;
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

    private void updateLimit(Endpoint endpoint, HttpResponse<String> response) {
        if (response.getStatus() == 429) {
            // We've reached a limit, eww
            final JSONObject retryResponse = new JSONObject(response.getBody());
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
        if(response.getHeaders().containsKey("X-RateLimit-Limit")) {
            final int remaining = Integer.parseInt(response.getHeaders().getFirst("X-RateLimit-Remaining"));
            final long reset = Long.parseLong(response.getHeaders().getFirst("X-RateLimit-Reset"));
            limiter.updateLimit(endpoint, reset, remaining);
        }
    }

    private void processHttpResponse(HttpResponse<String> response) {
        switch(response.getStatus()) {
            case 200:
            case 201:
            case 204:
                // everything is ok
                break;
            case 304: // Not modified
                if(response.getHeaders().getFirst("Content-Type").equals("application/json")) {
                    JSONObject object = new JSONObject(response.getBody());
                    logger.warn(object.getString("message"));
                    throw new DiscordRequestException(object.getString("message"), 304);
                }
                throw new DiscordRequestException("Unknown error has happened.", 304);
            default:
                // error, report it
                logger.error("HTTP request returned error code " + response.getStatus());
                throw new DiscordRequestException("Unknown error has happened.", response.getStatus());
        }
    }

    private HttpResponse<String> getRequestImpl(Endpoint endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending GET request to " + endpoint);

        GetRequest request = Unirest.get(endpoint.getAddress());
        fillHeaders(request);
        HttpResponse<String> response = null;
        try {
            response = request.asString();
            processHttpResponse(response);
        }
        catch (UnirestException e) {
            logger.warn("Couldn't send an object to discord servers.", e);
            return null;
        }
        finally {
            updateLimit(endpoint, response);
        }
        return response;
    }

    private void fireRequestImpl(Endpoint endpoint, HttpRequest request) {
        try {
            HttpResponse<String> response = request.asString();
            processHttpResponse(response);
            updateLimit(endpoint, response);
        }
        catch (UnirestException e) {
            logger.warn("Couldn't send an object to discord servers.", e);
        }
    }

    public synchronized void getRequest(Endpoint endpoint) {
        getRequestImpl(endpoint);
    }

    public synchronized JSONObject getRequestAsObject(Endpoint endpoint) {
        HttpResponse<String> response = getRequestImpl(endpoint);
        if(response == null) return null;
        return new JSONObject(response.getBody());
    }

    public synchronized JSONArray getRequestAsArray(Endpoint endpoint) {
        HttpResponse<String> response = getRequestImpl(endpoint);
        if(response == null) return null;
        return new JSONArray(response.getBody());
    }

    public synchronized void postRequest(Endpoint endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending empty POST request to " + endpoint);
        HttpRequestWithBody request = Unirest.post(endpoint.getAddress());
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void patchRequest(Endpoint endpoint, JSONObject object) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending PATCH request to " + endpoint);
        HttpRequestWithBody request = Unirest.patch(endpoint.getAddress());
        fillHeaders(request);
        request.body(object);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void putRequest(Endpoint endpoint) {
        if(stopped) return;
        // TODO: toASCIIstring()
        waitForLimit(endpoint);
        logger.debug("Sending PUT request to " + endpoint);
        HttpRequestWithBody request = Unirest.put(endpoint.getAddress());
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void deleteRequest(Endpoint endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending DELETE request to " + endpoint);
        HttpRequestWithBody request = Unirest.delete(endpoint.getAddress());
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void postRequest(Endpoint endpoint, JSONObject object) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending POST request to " + endpoint + ": " + object.toString());
        HttpRequestWithBody request = Unirest.post(endpoint.getAddress());
        fillHeaders(request);
        request.body(object);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void postRequest(Endpoint endpoint, MappedObject object) {
        if(stopped) return;
        postRequest(endpoint, serializer.serialize(object));
    }

    private <T extends HttpRequest> void fillHeaders(T request) {
        request.header("User-Agent", "DiscordBot (" + LIB_URL + ", " + LIB_VERSION + ")")
                .header("Authorization", "Bot " + token)
                .header("Content-Type", "application/json");
    }
}
