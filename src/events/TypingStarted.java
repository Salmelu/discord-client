package cz.salmelu.discord.events;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.User;

/**
 * An event triggered when an user starts or continues typing a message.
 */
public interface TypingStarted {
    /**
     * Gets the user typing the message.
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
