package cz.salmelu.discord.events;

import cz.salmelu.discord.resources.Game;
import cz.salmelu.discord.resources.PresenceStatus;
import cz.salmelu.discord.resources.User;

/**
 * <p>Event received when an user changes their presence.</p>
 * <p>The client application can receive multiple presence updates when it shares multiple
 * servers with the affected user.</p>
 */
public interface PresenceUpdate {
    /**
     * Gets affected user.
     * @return affected user
     */
    User getUser();

    /**
     * Gets user's current presence status.
     * @return user's current status
     */
    PresenceStatus getStatus();

    /**
     * Gets user's current nickname.
     * @return user's current nickname
     */
    String getNickname();

    /**
     * Gets the server id of the server, which triggered the presence update.
     * @return server id
     */
    String getServerId();

    /**
     * Gets the game currently played by the updated user
     * @return played game
     */
    Game getGame();
}
