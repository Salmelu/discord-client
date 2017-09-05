package cz.salmelu.discord.resources;

import cz.salmelu.discord.AsyncCallback;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.RequestResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * An extension of {@link Channel} to include server channel specific features.
 */
public interface ServerChannel extends Channel {
    /**
     * Gets channel's name.
     * @return channel name
     */
    String getName();

    /**
     * Gets channel's current topic.
     * @return channel topic or null for voice channels
     */
    String getTopic();

    /**
     * Checks if the channel is a voice channel
     * @return true if this channel is a voice channel
     */
    boolean isVoice();

    /**
     * Gets channel's position in channel list.
     * @return channel's position
     */
    int getPosition();

    /**
     * Gets channel's bitrate. Valid only for voice channels.
     * @return bitrate
     */
    int getBitrate();

    /**
     * Gets channel's user limit. Valid only for voice channels.
     * @return user limit
     */
    int getUserLimit();

    /**
     * Gets the server that this channel is part of.
     * @return channel's owning server
     */
    Server getServer();

    /**
     * Gets application's set of granted permissions for this channel.
     * @return set of granted permissions
     */
    Set<Permission> getPermissions();

    /**
     * <p>Changes channel's name.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newName a new channel's name (2-100 characters)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the name is of invalid length
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    Future<RequestResponse> changeName(String newName, AsyncCallback callback);

    /**
     * <p>Changes channel's topic.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newTopic a new channel's topic (0-1024 characters)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the topic is of invalid length
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a text channel
     */
    Future<RequestResponse> changeTopic(String newTopic, AsyncCallback callback);

    /**
     * <p>Changes channel's position in server channel list.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newPosition a new channel's position
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the position is a negative number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    Future<RequestResponse> changePosition(int newPosition, AsyncCallback callback);

    /**
     * <p>Changes channel's bitrate.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newBitRate a new channel's bitrate (8000-96000)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the bitrate is a not allowed number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a voice channel
     */
    Future<RequestResponse> changeBitrate(int newBitRate, AsyncCallback callback)
            throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Changes channel's user limit.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newUserLimit a new user limit (0-99)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the user limit is a not allowed number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a voice channel
     */
    Future<RequestResponse> changeUserLimit(int newUserLimit, AsyncCallback callback)
            throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Updates a text channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newName a new channel's name (2-100 characters)
     * @param newTopic a new channel's topic (0-1024 characters)
     * @param newPosition a new channel's position
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when either of the arguments is invalid
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    Future<RequestResponse> editTextChannel(String newName, String newTopic,
                                            int newPosition, AsyncCallback callback);

    /**
     * <p>Updates a voice channel</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newName a new channel's name (2-100 characters)
     * @param newPosition a new channel's position
     * @param newBitRate a new channel's bitrate (8000-96000)
     * @param newUserLimit a new user limit (0-99)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when either of the arguments is invalid
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    Future<RequestResponse> editVoiceChannel(String newName, int newPosition, int newBitRate,
                                             int newUserLimit, AsyncCallback callback);

    /**
     * <p>Updates channel permission overwrites</p>
     * <p>Replaces old overwrites with new ones.</p>
     * <p>To create new overwrites object, see {@link PermissionOverwriteBuilder}.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param old old channel's overwrites
     * @param replaced new channel's overwrites
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException when the overwrites type doesn't match
     * @throws PermissionDeniedException if the application doesn't have manage roles permission for this channel
     */
    Future<RequestResponse> updatePermissionOverwrites(PermissionOverwrite old, PermissionOverwrite replaced,
                                                       AsyncCallback callback);

    /**
     * <p>Delete channel's permission overwrites.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param overwrites deleted overwrites
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage roles permission for this channel
     */
    Future<RequestResponse> deletePermissionOverwrites(PermissionOverwrite overwrites, AsyncCallback callback)
            throws PermissionDeniedException;

    /**
     * <p>Gets a mention string for the channel.</p>
     * <p>This converts channel's name into a message specific string, which creates a channel link in other clients.</p>
     * <p>Use this string in a message if you wish to link a channel.</p>
     * @return a string for linking the channel
     */
    String getMention();

    /**
     * <p>Deletes a list of messages in the channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param messages a list of deleted messages
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage messages permission for this channel
     */
    Future<RequestResponse> bulkDeleteMessages(List<Message> messages, AsyncCallback callback);

    /**
     * <p>Deletes a list of messages in the channel specified by their ids.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param messageIds a list of deleted messages' ids
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage messages permission for this channel
     */
    Future<RequestResponse> bulkDeleteMessagesByIds(List<String> messageIds, AsyncCallback callback);
}
