package cz.salmelu.discord.resources;

/**
 * <p>Represents a game instance played by Discord users.</p>
 * <p>Discord doesn't enforce the game is real, therefore this can contain anything.</p>
 */
public interface Game {
    /**
     * Gets name of the game played by the user.
     * @return game name
     */
    String getName();

    /**
     * Checks whether the user is streaming the game they are playing
     * @return true if the user is streaming
     */
    boolean isStreaming();

    /**
     * If the user is streaming, this gets the URL of the stream.
     * @return URL of the stream or null, if the user isn't streaming
     */
    String getURL();
}
