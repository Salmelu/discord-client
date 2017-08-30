package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

import java.util.List;
import java.util.Set;

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
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newName a new channel's name (2-100 characters)
     * @throws IllegalArgumentException when the name is of invalid length
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    void changeName(String newName) throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Changes channel's topic.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newTopic a new channel's topic (0-1024 characters)
     * @throws IllegalArgumentException when the topic is of invalid length
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a text channel
     */
    void changeTopic(String newTopic) throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Changes channel's position in server channel list.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newPosition a new channel's position
     * @throws IllegalArgumentException when the position is a negative number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    void changePosition(int newPosition) throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Changes channel's bitrate.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newBitRate a new channel's bitrate (8000-96000)
     * @throws IllegalArgumentException when the bitrate is a not allowed number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a voice channel
     */
    void changeBitrate(int newBitRate) throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Changes channel's user limit.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newUserLimit a new user limit (0-99)
     * @throws IllegalArgumentException when the user limit is a not allowed number
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     * or if the channel is not a voice channel
     */
    void changeUserLimit(int newUserLimit) throws IllegalArgumentException, PermissionDeniedException;

    /**
     * <p>Updates a text channel.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newName a new channel's name (2-100 characters)
     * @param newTopic a new channel's topic (0-1024 characters)
     * @param newPosition a new channel's position
     * @throws IllegalArgumentException when either of the arguments is invalid
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    void editTextChannel(String newName, String newTopic, int newPosition);

    /**
     * <p>Updates a voice channel</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param newName a new channel's name (2-100 characters)
     * @param newPosition a new channel's position
     * @param newBitRate a new channel's bitrate (8000-96000)
     * @param newUserLimit a new user limit (0-99)
     * @throws IllegalArgumentException when either of the arguments is invalid
     * @throws PermissionDeniedException if the application doesn't have manage channel permission for this channel
     */
    void editVoiceChannel(String newName, int newPosition, int newBitRate, int newUserLimit);

    /**
     * <p>Updates channel permission overwrites</p>
     * <p>Replaces old overwrites with new ones.</p>
     * <p>To create new overwrites object, see {@link PermissionOverwriteBuilder}.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param old old channel's overwrites
     * @param replaced new channel's overwrites
     * @throws IllegalArgumentException when the overwrites type doesn't match
     * @throws PermissionDeniedException if the application doesn't have manage roles permission for this channel
     */
    void updatePermissionOverwrites(PermissionOverwrite old, PermissionOverwrite replaced)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Delete channel's permission overwrites.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param overwrites deleted overwrites
     * @throws PermissionDeniedException if the application doesn't have manage roles permission for this channel
     */
    void deletePermissionOverwrites(PermissionOverwrite overwrites) throws PermissionDeniedException;

    /**
     * <p>Gets a mention string for the channel.</p>
     * <p>This converts channel's name into a message specific string, which creates a channel link in other clients.</p>
     * <p>Use this string in a message if you wish to link a channel.</p>
     * @return a string for linking the channel
     */
    String getMention();

    /**
     * <p>Deletes a list of messages in the channel.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param messages a list of deleted messages
     * @throws PermissionDeniedException if the application doesn't have manage messages permission for this channel
     */
    void bulkDeleteMessages(List<Message> messages);

    /**
     * <p>Deletes a list of messages in the channel specified by their ids.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param messageIds a list of deleted messages' ids
     * @throws PermissionDeniedException if the application doesn't have manage messages permission for this channel
     */
    void bulkDeleteMessagesByIds(List<String> messageIds);
}
