package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.RequestResponse;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * A Discord community server. Officially called a <i>Guild</i> in Discord documentation,
 * however, this library uses the name with which most Discord users are more familiar with.
 */
public interface Server {
    boolean isDisabled();

    /**
     * Gets server's unique id.
     * @return unique id
     */
    String getId();

    /**
     * Gets server's name given to it by it's owner.
     * @return server's name
     */
    String getName();

    /**
     * <p>Leaves the server.</p>
     * <p><b>Attention: </b>This cannot be taken back. The only way to get back to the server is asking
     * one of the administrators to invite the application back.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     */
    Future<RequestResponse> leave();

    /**
     * Gets the list of channels present on this server.
     * @return list of server channels
     */
    List<ServerChannel> getChannels();

    /**
     * Gets a specific channel by its id.
     * @param id channel id
     * @return channel or null, if such channel is not present
     */
    ServerChannel getChannelById(String id);

    /**
     * Gets a specific channel by its name.
     * @param name channel name
     * @return channel or null, if such channel is not present
     */
    ServerChannel getChannelByName(String name);

    /**
     * Gets the special @everyone role which is assigned to every user.
     * @return @everyone role
     */
    Role getEveryoneRole();

    /**
     * <p>Gets the set of permissions this application has been granted.</p>
     * <p>The permissions can be different in each channel so if operating on a specific channel,
     * use {@link ServerChannel#getPermissions()} instead.</p>
     * @return set of granted permissions
     */
    Set<Permission> getPermissions();

    /**
     * Gets a list containing all roles on the server.
     * @return list of all roles
     */
    List<Role> getRoles();

    /**
     * Gets a specific role distinguished by its id
     * @param id role id
     * @return a role instance or null if it doesn't exist
     */
    Role getRoleById(String id);

    /**
     * Gets a specific role by its name
     * @param name role name
     * @return a role instance or null if it doesn't exist
     */
    Role getRoleByName(String name);

    /**
     * <p>Requests the server to load all members. By default, the server can only send the online members when
     * it's first loaded.</p>
     * <p>This method sends a request to Discord Gateway and doesn't await response. The member list may be received
     * using {@link cz.salmelu.discord.listeners.ServerListener#onMemberChunk(List)} method.</p>
     */
    void loadAllMembers();

    /**
     * <p>Gets a list of all members on the server.</p>
     * <p><i>Attention:</i> some members may not be loaded, see {@link #loadAllMembers()}</p>
     * @return a list of loaded members of this server
     */
    List<Member> getMembers();

    /**
     * <p>Gets the member with given id</p>
     * <p><i>Attention:</i> the member may not be loaded, see {@link #loadAllMembers()}</p>
     * @param id member id
     * @return a member with given id or null, if there is none
     */
    Member getMemberById(String id);

    /**
     * <p>Gets the member with given name</p>
     * <p><i>Attention:</i> the member may not be loaded, see {@link #loadAllMembers()}</p>
     * @param nickname member nickname
     * @return a member with given nickname or null, if there is none
     */
    Member getMemberByNickname(String nickname);

    /**
     * <p>Converts an user instance into member instance.</p>
     * <p><i>Attention:</i> the member may not be loaded, see {@link #loadAllMembers()}</p>
     * @param user converted user
     * @return a member with representing the given user on the server or null, if there is none
     */
    Member getMember(User user);

    /**
     * <p>Kicks the member from the server. The user will be able to rejoin again if they receive an invite.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have kick members permission
     */
    Future<RequestResponse> kickMember(Member member) throws PermissionDeniedException;

    /**
     * <p>Gets list of all banned users from this server.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @return list of banned users
     * @throws PermissionDeniedException if the application doesn't have ban members permission
     */
    List<User> getBannedUsers() throws PermissionDeniedException;

    /**
     * <p>Bans the member from the server. The member will not be able to rejoin the server until they
     * are unbanned.</p>
     * <p>Optionally, this call can delete all messages sent by the member in last few days.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param member banned member
     * @param messageDays how old messages will be deleted (valid values are 0-7, where 0 means no messages
     *                    will be deleted)
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have ban members permission
     * @throws IllegalArgumentException if the parameter has invalid value
     */
    Future<RequestResponse> banMember(Member member, int messageDays)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Bans the user from the server. The user will not be able to rejoin the server until they
     * are unbanned.</p>
     * <p>Optionally, this call can delete all messages sent by the user in last few days.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param user banned user
     * @param messageDays how old messages will be deleted (valid values are 0-7, where 0 means no messages
     *                    will be deleted)
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have ban members permission
     * @throws IllegalArgumentException if the parameter has invalid value
     */
    Future<RequestResponse> banUser(User user, int messageDays)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Unbans a banned user. The user can then use an invite to rejoin the server.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param user banned user
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have ban members permission
     */
    Future<RequestResponse> unbanUser(User user) throws PermissionDeniedException;

    /**
     * <p>Changes the application's user's nickname.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param nickname new nickname
     * @return future for obtaining the response from Discord servers
     * @throws IllegalArgumentException if the nickname is not valid by Discord standards,
     * see {@link cz.salmelu.discord.NameHelper#validateName(String)}
     */
    Future<RequestResponse> changeMyNickname(String nickname);

    /**
     * <p>Creates a new text channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param name channel name
     * @param overwrites permission overwrites for the channel
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage channels permission
     */
    Future<RequestResponse> createTextChannel(String name, List<PermissionOverwrite> overwrites)
            throws PermissionDeniedException;

    /**
     * <p>Creates a new voice channel.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param name channel name
     * @param overwrites permission overwrites for the channel
     * @param bitrate bitrate of the channel
     * @param userLimit maximum amount of users that can be in the channel simultaneously
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage channels permission
     */
    Future<RequestResponse> createVoiceChannel(String name, int bitrate, int userLimit,
                                               List<PermissionOverwrite> overwrites)
            throws PermissionDeniedException;

    /**
     * <p>Deletes an existing channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param channel deleted channel
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage channels permission
     * @throws IllegalArgumentException if the channel is not a part of this server
     */
    Future<RequestResponse> deleteChannel(ServerChannel channel)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Creates a new role.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param name role name
     * @param permissions a list of permissions the role will be granted
     * @param color role's color
     * @param separate set to true if the role should be displayed separately
     * @param mentionable set to true if the members should be able to mention this role
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> createRole(String name, Collection<Permission> permissions, int color, boolean separate,
                                       boolean mentionable);

    /**
     * <p>Updates an existing role.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param role updated role
     * @param name role name
     * @param permissions a list of permissions the role will be granted
     * @param color role's color
     * @param separate set to true if the role should be displayed separately
     * @param mentionable set to true if the members should be able to mention this role
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> updateRole(Role role, String name, Collection<Permission> permissions, int color, boolean separate,
                                       boolean mentionable);

    /**
     * <p>Deletes a role from the server.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param role deleted role
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> deleteRole(Role role)
            throws PermissionDeniedException;

    /**
     * <p>Checks how many members would be kicked if {@link #pruneMembers(int)} was called with this argument.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param days the minimum amount of days the user must be inactive for them to be kicked
     * @return the number of potentially kicked members
     * @throws PermissionDeniedException if the application doesn't have kick members permission
     * @throws IllegalArgumentException when the argument is a non-positive number
     */
    int getPruneMembersCount(int days) throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Mass kicks inactive members.</p>
     * <p>This method sends a request to Discord server and therefore the call blocks until it's completed.</p>
     * @param days the minimum amount of days the user must be inactive for them to be kicked
     * @return the number of kicked members
     * @throws PermissionDeniedException if the application doesn't have kick members permission
     * @throws IllegalArgumentException when the argument is a non-positive number
     */
    int pruneMembers(int days) throws PermissionDeniedException, IllegalArgumentException;
}
