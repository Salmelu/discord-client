package cz.salmelu.discord.listeners;

import cz.salmelu.discord.events.PresenceUpdate;
import cz.salmelu.discord.events.TypingStarted;
import cz.salmelu.discord.resources.PrivateChannel;
import cz.salmelu.discord.resources.User;

/**
 * <p>An interface with user-related callbacks (such as user changes, private channels, etc.)
 * for modules.</p>
 *
 * <p>A module shall implement this interface when it needs to react to user changes.</p>
 */
public interface UserActionListener {

    /**
     * Called when an user starts typing a message.
     * @param typingEvent object with metadata about the event
     */
    default void onTypingStart(TypingStarted typingEvent) {

    }

    /**
     * Called when an user changes their presence.
     * @param presenceEvent object with metadata about the event
     */
    default void onPresenceChange(PresenceUpdate presenceEvent) {

    }

    /**
     * Called when an user updated his profile information.
     * @param updatedUser changed user
     */
    default void onUserUpdate(User updatedUser) {

    }

    /**
     * Called when a private channel is opened.
     * @param channel opened channel
     */
    default void onChannelOpen(PrivateChannel channel) {

    }

    /**
     * Called when a private channel is closed.
     * @param channel closed channel
     */
    default void onChannelClose(PrivateChannel channel) {

    }
}
