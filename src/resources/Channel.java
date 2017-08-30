package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

import java.util.List;

/**
 * Represents a generic Discord channel.
 *
 * If needed, the channel can be cast into private or server channel. See {@link #isPrivate()} method.
 */
public interface Channel {
    /**
     * Gets channel's unique id. This id is set by Discord and cannot be ever changed.
     * @return channel's id
     */
    String getId();

    /**
     * <p>Checks if the channel is a private (direct) message channel.</p>
     *
     * <p>Use {@link #toPrivateChannel()} to cast this channel into {@link PrivateChannel},
     * or {@link #toServerChannel()} to cast the channel into {@link ServerChannel}.</p>
     * @return true, if the channel is a private message channel
     */
    boolean isPrivate();

    /**
     * <p>Checks, whether the application can send messages into the channel.</p>
     * <p>Always evaluates to true for private channels.</p>
     * @return true, if the application has permission to post into this channel
     */
    boolean canSendMessage();

    /**
     * <p>Sends a new message into this channel.</p>
     * <p>This method sends the message over network and will block until the message is sent.</p>
     * @param text sent text message
     * @throws PermissionDeniedException if the application doesn't have send message permissions
     */
    void sendMessage(String text) throws PermissionDeniedException;

    /**
     * <p>Gets a message in this channel with given id.</p>
     * <p>If the message is not yet cached, the method blocks until the message is retrieved from Discord servers.</p>
     * @param id desired message's id
     * @return message in channel with given id, or null if there is no message with such id
     * @throws PermissionDeniedException if the application cannot read message history
     */
    Message getMessage(String id) throws PermissionDeniedException;

    /**
     * <p>Gets a group of messages sent before message with a given id.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param messageId the borderline message id
     * @param limit maximum amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    List<Message> getMessagesBefore(String messageId, int limit) throws PermissionDeniedException;

    /**
     * <p>Gets a group of messages sent around message with a given id.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param messageId the borderline message id
     * @param limit maximum amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    List<Message> getMessagesAround(String messageId, int limit) throws PermissionDeniedException;

    /**
     * <p>Gets a group of messages sent after message with a given id.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param messageId the borderline message id
     * @param limit maximum amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    List<Message> getMessagesAfter(String messageId, int limit) throws PermissionDeniedException;

    /**
     * <p>Gets a group of messages sent before a specific message.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param message the borderline message
     * @param limit amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    default List<Message> getMessagesBefore(Message message, int limit) throws PermissionDeniedException {
        return getMessagesBefore(message.getId(), limit);
    }

    /**
     * <p>Gets a group of messages sent around a specific message.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param message the borderline message
     * @param limit amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    default List<Message> getMessagesAround(Message message, int limit) throws PermissionDeniedException {
        return getMessagesAround(message.getId(), limit);
    }

    /**
     * <p>Gets a group of messages sent after a specific message.</p>
     * <p>This call always queries Discord servers and blocks until the messages are received.</p>
     * @param message the borderline message
     * @param limit amount of messages requested, 1-100 allowed
     * @return list of messages in the channel
     * @throws PermissionDeniedException if the application cannot read message history
     */
    default List<Message> getMessagesAfter(Message message, int limit) throws PermissionDeniedException {
        return getMessagesAfter(message.getId(), limit);
    }

    /**
     * <p>Notifies Discord that the client is typing a message.</p>
     * <p>This triggers the <i>User is typing...</i> message for other users.</p>
     */
    void triggerTyping();

    /**
     * Casts the channel into {@link ServerChannel} instance.
     * @return a {@link ServerChannel} instance or null, if the channel is not a server channel
     */
    ServerChannel toServerChannel();

    /**
     * Casts the channel into {@link PrivateChannel} instance.
     * @return a {@link PrivateChannel} instance or null, if the channel is not a private channel
     */
    PrivateChannel toPrivateChannel();

    /**
     * <p>Gets channel's pinned messages.</p>
     * <p>Blocks until the request is finished.</p>
     * @return a list of pinned messages in the channel
     */
    List<Message> getPinnedMessages();

    /**
     * <p>Pins a specific message in the channel.</p>
     * <p>Blocks until the request is finished.</p>
     * @param message pinned message
     * @throws PermissionDeniedException if the channel is a server channel and application doesn't have
     * manage messages permission
     */
    void pinMessage(Message message) throws PermissionDeniedException;

    /**
     * <p>Unpins a specific message in the channel.</p>
     * <p>Blocks until the request is finished.</p>
     * @param message unpinned message
     * @throws PermissionDeniedException if the channel is a server channel and application doesn't have
     * manage messages permission
     */
    void unpinMessage(Message message) throws PermissionDeniedException;

    /**
     * <p>Deletes this channel.</p>
     * <p>If the channel is a private channel, this call only closes the channel.</p>
     * <p>Blocks until the request is finished.</p>
     * @throws PermissionDeniedException if the channel is a server channel and application doesn't have
     * manage channels permission
     */
    void deleteChannel() throws PermissionDeniedException;

    class ChannelType {
        public static final int SERVER_TEXT = 0;
        public static final int PRIVATE = 1;
        public static final int SERVER_VOICE = 2;
        public static final int PRIVATE_GROUP = 3;
        public static final int SERVER_CATEGORY = 4;
    }
}
