package cz.salmelu.discord.resources;

import cz.salmelu.discord.AsyncCallback;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.RequestResponse;

import java.util.List;
import java.util.concurrent.Future;

/**
 * <p>A server member on a single specific server.</p>
 * <p>Server member is an user's identity on given server with associated permissions and roles.</p>
 */
public interface Member {
    /**
     * <p>Gets member's id. It is equivalent to their respective user id.</p>
     * <p>This id is set by Discord and cannot be ever changed.</p>
     * @return member's unique id
     */
    String getId();

    /**
     * <p>Gets the {@link User} instance of this member.</p>
     * @return member's user instance
     */
    User getUser();

    /**
     * Gets the member's current nickname on the server, if it's set.
     * @return member's nickname or null
     */
    String getNickname();

    /**
     * Gets the list of member's current roles.
     * @return list of member's roles.
     */
    List<Role> getRoles();

    /**
     * <p>Gets a mention string for the member.</p>
     * <p>This converts member's name and discriminator into a message specific string, which triggers a mention.</p>
     * <p>Use this string in a message if you wish to mention the member.</p>
     * @return a string for mentioning the member
     */
    String getMention();

    /**
     * Gets the server of this member.
     * @return a server instance
     */
    Server getServer();

    /**
     * <p>Creates a private channel in order to send private messages to this member.</p>
     * <p>This issues a request to Discord server and therefore it blocks until the request is completed.</p>
     * @return a new instance of private channel
     */
    PrivateChannel createPrivateChannel();

    /**
     * <p>Assigns a role to the member.</p>
     * <p>If such role doesn't exist on the server, this method has no effect and returns null.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param role assigned role
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers, or null if the role doesn't exist
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> addRole(Role role, AsyncCallback callback) throws PermissionDeniedException;

    /**
     * <p>Removes a role from the member.</p>
     * <p>If such role doesn't exist on the server, this method has no effect.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param role removed role
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers, or null if the role doesn't exist
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> removeRole(Role role, AsyncCallback callback) throws PermissionDeniedException;

    /**
     * <p>Sets member's roles to given roles.</p>
     * <p>In other words, this removes all member's roles and assigns only those specific roles to them.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param roles list of assigned roles
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage roles permission
     */
    Future<RequestResponse> setRoles(List<Role> roles, AsyncCallback callback)
            throws PermissionDeniedException;

    /**
     * <p>Mutes or unmutes the member for all voice channels.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param mute if true, the member is muted, if false, the member is unmuted
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have mute members permission
     */
    Future<RequestResponse> mute(boolean mute, AsyncCallback callback)
            throws PermissionDeniedException;

    /**
     * <p>Deafens the member on all voice channels.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param deaf if true, the member is deafened, if false, the condition is cancelled
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have deafen members permission
     */
    Future<RequestResponse> deafen(boolean deaf, AsyncCallback callback)
            throws PermissionDeniedException;

    /**
     * <p>Moves the member into different voice channel.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param newChannel channel the member is moved into
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have move members permission
     * @throws IllegalArgumentException if the channel is not a voice channel or not a part of this server
     */
    Future<RequestResponse> moveChannel(ServerChannel newChannel, AsyncCallback callback)
            throws PermissionDeniedException, IllegalArgumentException;

    /**
     * <p>Bans the member from the server. The member will not be able to rejoin the server until they
     * are unbanned.</p>
     * <p>Optionally, the call can delete all messages sent by the user in last few days.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param messageDays how old messages will be deleted (valid values are 0-7, where 0 means no messages
     *                    will be deleted)
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have ban members permission
     * @throws IllegalArgumentException if the parameter has invalid value
     */
    Future<RequestResponse> ban(int messageDays, AsyncCallback callback) throws PermissionDeniedException;

    /**
     * <p>Kicks the member from the server. The user will be able to rejoin again if they receive an invite.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have kick members permission
     */
    Future<RequestResponse> kick(AsyncCallback callback) throws PermissionDeniedException;

    /**
     * <p>Forcefully changes nickname of this member.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param nickname new member's nickname
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException if the application doesn't have manage nicknames permission
     * @throws IllegalArgumentException if the nickname is not valid by Discord standards,
     * see {@link cz.salmelu.discord.NameHelper#validateName(String)}
     */
    Future<RequestResponse> changeNickname(String nickname, AsyncCallback callback)
            throws PermissionDeniedException, IllegalArgumentException;
}
