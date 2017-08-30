package cz.salmelu.discord.resources;

import cz.salmelu.discord.Emoji;

/**
 * A reaction attached to a channel message.
 */
public interface Reaction {
    /**
     * Gets the amount of users that added this reaction to the message.
     * @return amount of reacting users
     */
    int getCount();

    /**
     * Checks if the current application added the reaction.
     * @return true if the application added the reaction
     */
    boolean isMine();

    /**
     * Gets the emoji used in this reaction.
     * @return used emoji
     */
    Emoji getEmoji();

    /**
     * Gets the message to which this reaction is attached to.
     * @return relevant message instance
     */
    Message getMessage();
}
