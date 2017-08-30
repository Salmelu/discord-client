package cz.salmelu.discord.resources;

/**
 * <p>Represents a deleted message.</p>
 * <p>Contains only id of the message and its channel (if accessible),
 * because all other information may be lost if the message is not cached on the client.</p>
 * <p>If your application needs to track specific messages, it needs to save them itself.</p>
 */
public interface DeletedMessage {
    /**
     * Gets the id of the deleted message.
     * @return id of deleted message
     */
    String getId();

    /**
     * Gets the channel where the deleted message was originally posted.
     * @return a channel instance or null, if it's not present on client
     */
    Channel getChannel();
}
