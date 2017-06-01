package cz.salmelu.discord.implementation.net;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import cz.salmelu.discord.implementation.json.JSONMappedObject;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker marker = MarkerFactory.getMarker("HttpRequester");

    public DiscordHttpRequester(String token, RateLimiter limiter) {
        this.token = token;
        this.limiter = limiter;
    }

    public void stop() {
        this.stopped = true;
    }

    public HttpResponse<String> getRequestImpl(String endpoint) {
        GetRequest request = Unirest.get(endpoint);
        fillHeaders(request);
        try {
            return request.asString();
        }
        catch (UnirestException e) {
            logger.warn(marker, "Couldn't send an object to discord servers.", e);
        }
        return null;
    }

    private void waitForLimit(String endpoint) {
        long waitFor = limiter.checkLimit(endpoint);
        while(waitFor != 0) {
            logger.warn(marker, "Request to endpoint " + endpoint + " is being rate limited, " +
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
                    logger.warn(marker, object.getString("message"));
                }
                break;
            default:
                // error, report it
                logger.error(marker, "HTTP request returned error code " + response.getStatus());
        }
    }

    public synchronized void getRequest(String endpoint) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug(marker, "Sending GET request to " + endpoint);
        HttpResponse<String> response = getRequestImpl(endpoint);
        processHttpResponse(response);
        updateLimit(endpoint, response);
    }

    public synchronized JSONObject getRequestAsObject(String endpoint) {
        if(stopped) return null;
        waitForLimit(endpoint);
        logger.debug(marker, "Sending GET request to " + endpoint);
        HttpResponse<String> response = getRequestImpl(endpoint);
        processHttpResponse(response);
        updateLimit(endpoint, response);
        return new JSONObject(response.getBody());
    }

    public synchronized void postRequest(String endpoint) {
        if(stopped) return;

    }

    public synchronized void postRequest(String endpoint, JSONObject object) {
        if(stopped) return;
        waitForLimit(endpoint);
        logger.debug(marker, "Sending POST request to " + endpoint + ": " + object.toString());
        HttpRequestWithBody request = Unirest.post(endpoint);
        fillHeaders(request);
        request.body(object);
        try {
            HttpResponse<String> response = request.asString();
            processHttpResponse(response);
            updateLimit(endpoint, response);
        }
        catch (UnirestException e) {
            logger.warn(marker, "Couldn't send an object to discord servers.", e);
        }

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
