package cz.salmelu.discord;

/**
 * <p>An exception thrown when the bot account doesn't have the required permissions
 * to perform the requested operation.</p>
 */
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
