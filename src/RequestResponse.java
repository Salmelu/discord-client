package cz.salmelu.discord;

import java.util.concurrent.Future;

/**
 * <p>A response to an asynchronous request to Discord servers.</p>
 * <p>This can be used to check the response and react accordingly.</p>
 */
public interface RequestResponse {
    /**
     * Checks if the request was successful and returned no error.
     * @return true if the request was successful
     */
    boolean isSuccessful();

    /**
     * Gets HTTP status code received by the server as a reaction to the request.
     * @return response status code
     */
    int getStatusCode();

    /**
     * Gets HTTP status message received by the server as a reaction to the request.
     * @return response status message
     */
    String getStatusMessage();

    /**
     * <p>Checks, if the request failed because of rate limitation.</p>
     * <p>If this happens, you probably used too many similar requests (like add reaction) one after another.
     * If this happens because of spamming requests, try waiting after each one with {@link Future#get()}.
     * If it happens randomly, it can be because of different modules doing same actions. Simply try again.</p>
     * <p><b>Warning:</b> if you go over rates too much, you risk getting banned.</p>
     * @return true if the request failed due to rate limit
     */
    boolean isRateLimited();
}
