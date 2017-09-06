package cz.salmelu.discord.events;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.User;

/**
 * An event triggered when an user starts or continues typing a message.
 */
public interface TypingStarted {
    /**
     * <p>Gets the user typing the message.</p>
     * <p><i>Notice:</i> if the user is not loaded, this will trigger a request to servers and will block
     * until it is completed.</p>
     * @return typing user
     */
    User getUser();

    /**
     * Gets the channel where the message is typed into
     * @return affected channel
     */
    Channel getChannel();

    /**
     * Gets the timestamp of the event
     * @return event timestamp
     */
    long getTimestamp();
}
