package cz.salmelu.discord.implementation.net;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import cz.salmelu.discord.implementation.json.JSONMappedObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DiscordHttpRequester {

    private static final String LIB_URL = "salmelu.cz";
    private static final String LIB_VERSION = "0.0.1";
    private final String token;
    private final RateLimiter limiter;

    private boolean stopped = false;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public DiscordHttpRequester(String token, RateLimiter limiter) {
        this.token = token;
        this.limiter = limiter;
    }

    public void stop() {
        this.stopped = true;
    }

    private void waitForLimit(String endpoint) {
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

    private void updateLimit(String endpoint, HttpResponse<String> response) {
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

    private HttpResponse<String> getRequestImpl(String endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug("Sending GET request to " + endpoint);

        GetRequest request = Unirest.get(endpoint);
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

    private void fireRequestImpl(String endpoint, HttpRequest request) {
        try {
            HttpResponse<String> response = request.asString();
            processHttpResponse(response);
            updateLimit(endpoint, response);
        }
        catch (UnirestException e) {
            logger.warn("Couldn't send an object to discord servers.", e);
        }
    }

    public synchronized void getRequest(String endpoint) {
        getRequestImpl(endpoint);
    }

    public synchronized JSONObject getRequestAsObject(String endpoint) {
        HttpResponse<String> response = getRequestImpl(endpoint);
        if(response == null) return null;
        return new JSONObject(response.getBody());
    }

    public synchronized JSONArray getRequestAsArray(String endpoint) {
        HttpResponse<String> response = getRequestImpl(endpoint);
        if(response == null) return null;
        return new JSONArray(response.getBody());
    }

    public synchronized void postRequest(String endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending empty POST request to " + endpoint);
        HttpRequestWithBody request = Unirest.post(endpoint);
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void patchRequest(String endpoint, JSONObject object) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending PATCH request to " + endpoint);
        HttpRequestWithBody request = Unirest.patch(endpoint);
        fillHeaders(request);
        request.body(object);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void putRequest(String endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending PUT request to " + endpoint);
        HttpRequestWithBody request = Unirest.put(endpoint);
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void deleteRequest(String endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending DELETE request to " + endpoint);
        HttpRequestWithBody request = Unirest.delete(endpoint);
        fillHeaders(request);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void postRequest(String endpoint, JSONObject object) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug("Sending POST request to " + endpoint + ": " + object.toString());
        HttpRequestWithBody request = Unirest.post(endpoint);
        fillHeaders(request);
        request.body(object);
        fireRequestImpl(endpoint, request);
    }

    public synchronized void postRequest(String endpoint, JSONMappedObject object) {
        if(stopped) return;
        postRequest(endpoint, object.serialize());
    }

    private <T extends HttpRequest> void fillHeaders(T request) {
        request.header("User-Agent", "DiscordBot (" + LIB_URL + ", " + LIB_VERSION + ")")
                .header("Authorization", "Bot " + token)
                .header("Content-Type", "application/json");
    }
}
