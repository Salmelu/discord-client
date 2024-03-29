package cz.salmelu.discord;

/**
 * <p>An exception received when a request to Discord server fails.</p>
 */
public class DiscordRequestException extends RuntimeException {

    private final int responseCode;

    public DiscordRequestException(String s, int responseCode) {
        super(s);
        this.responseCode = responseCode;
    }

    /**
     * Gets HTTP status code contained in the HTTP response sent by server.
     * @return HTTP response status code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Checks if the exception was thrown because of going over rate limit.
     * @return true if the call failed due to rate limits
     */
     public boolean isRateLimit() {
        return responseCode == 429;
     }
}
