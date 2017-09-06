package cz.salmelu.discord.resources;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.RequestResponse;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * <p>A message received by Discord servers.</p>
 */
public interface Message {
    /**
     * <p>Gets message's unique id. This cannot be changed.</p>
     * @return message's id
     */
    String getId();

    /**
     * Gets the text the way it was received by the client, with all mentions in the raw format.
     * @return the raw message text
     */
    String getRawText();

    /**
     * Gets the message's text with converted mentions back into @names.
     * @return converted message text
     */
    String getText();

    /**
     * <p>Changes the text of application's message into a different text.</p>
     * <p>Due to the limitations of Discord, the application can only change its own messages.</p>
     * <p>This method sends an asynchronous request to Discord server and doesn't await completion. Use the
     * returned future to check for completion.</p>
     * @param newText text replacing the old text
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException when the message is not owned by the application
     */
    Future<RequestResponse> edit(String newText);

    /**
     * <p>Deletes the message from the channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the message is someone else's and the application
     * doesn't have manage messages permission in relevant channel
     */
    Future<RequestResponse> delete() throws PermissionDeniedException;

    /**
     * Gets the instance of the channel where the message was posted
     * @return channel where the message was posted
     */
    Channel getChannel();

    /**
     * Gets the collection of all reactions attached to the message
     * @return collection of attached reactions
     */
    Collection<Reaction> getReactions();

    /**
     * <p>Adds a new reaction to the message.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param emoji added reaction
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException either when the application doesn't have read message history permission,
     * or when it doesn't have add reactions permission and there is no reaction of the same type present
     * @throws IllegalArgumentException when attempting to add the same reaction twice
     */
    Future<RequestResponse> addReaction(Emoji emoji)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Removes a previously added reaction from the message.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param emoji removed reaction
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the reaction wasn't added before
     */
    Future<RequestResponse> removeReaction(Emoji emoji) throws IllegalArgumentException;

    /**
     * <p>Removes another user's reaction from the message.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param emoji removed reaction
     * @param user author of the removed reaction
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage messages permission
     */
    Future<RequestResponse> removeUserReaction(Emoji emoji, User user) throws PermissionDeniedException;

    /**
     * <p>Removes all reactions from the message.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage messages permission
     */
    Future<RequestResponse> removeAllReactions() throws PermissionDeniedException;

    /**
     * <p>Gets a list of those users who added the given reaction to the message.</p>
     * <p>This method sends a request to Discord server and therefore it blocks until it's completed.</p>
     * @param emoji polled reaction
     * @return list of reacting users
     */
    List<User> getReactions(Emoji emoji);

    /**
     * Gets the user who sent the message.
     * @return author of the message
     */
    User getAuthor();

    /**
     * Gets the time when the message was originally sent.
     * @return sent time
     */
    OffsetDateTime getSentTime();

    /**
     * Gets the time when the message was last edited.
     * @return last edited time or null, if the message was not edited
     */
    OffsetDateTime getEditedTime();

    /**
     * Checks if the message is a text-to-speech message.
     * @return true when the message is a TTS message
     */
    boolean isTTS();

    /**
     * Checks if the message contains a mention @everyone.
     * @return true if message mentions everyone
     */
    boolean isMentionAtEveryone();

    /**
     * Gets a list of all users who were mentioned in the message.
     * @return list of mentioned users
     */
    List<User> getMentionedUsers();

    /**
     * Gets a list of all roles That were mentioned in the message.
     * @return list of mentioned roles
     */
    List<Role> getMentionedRoles();

    /**
     * <p>Sends a message into the same channel where this message is originally from.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param reply text of the message
     * @throws PermissionDeniedException when the application doesn't have send messages permission in the channel
     */
    Future<RequestResponse> reply(String reply) throws PermissionDeniedException;

    /**
     * <p>Pins the message in its channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException when the application doesn't have manage messages permission in the channel
     */
    Future<RequestResponse> pin() throws PermissionDeniedException;

    /**
     * <p>Unpins the message from its channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException when the application doesn't have manage messages permission in the channel
     */
    Future<RequestResponse> unpin() throws PermissionDeniedException;
}
