package cz.salmelu.discord.resources;

import cz.salmelu.discord.AsyncCallback;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.RequestResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * A role created on a server that is given to its members in order to give them specific permissions.
 */
public interface Role {

    /**
     * Gets role's unique id.
     * @return role's id
     */
    String getId();

    /**
     * Gets role's name as it was named by it's creator.
     * @return role's name
     */
    String getName();

    /**
     * Gets the server which the role is part of.
     * @return server having this role
     */
    Server getServer();

    /**
     * <p>Gets a mention string for the role.</p>
     * <p>This converts the role name into a message specific string, which triggers a mention.</p>
     * <p>Use this string in a message if you wish to mention all users with this role.</p>
     * @return specific mention string or null, if the role cannot be mentioned
     */
    String getMention();

    /**
     * <p>Gets the role's color.</p>
     * <p>The role's color is used by the client when displaying the members in the member list.
     * The used color is the color of the highest role the user has assigned.</p>
     * @return the role's color, encoded in integer
     */
    int getColor();

    /**
     * Checks if the role can be mentioned by server members.
     * @return true if the role can be mentioned in messages
     */
    boolean isMentionable();

    /**
     * Checks if the role is displayed separately from the other roles in the online member list.
     * @return true if the role is displayed separately
     */
    boolean isSeparate();

    /**
     * Gets a set of permissions the members with this role are granted.
     * @return set of permissions
     */
    Set<Permission> getPermissions();

    /**
     * <p>Updates the role with new values.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param name new role name
     * @param permissions new role permissions
     * @param color new role color
     * @param separate whether the role displays separately
     * @param mentionable whether the role can be mentioned
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException when the application doesn't have manage roles permission
     */
    Future<RequestResponse> update(String name, List<Permission> permissions, int color, boolean separate,
                                   boolean mentionable, AsyncCallback callback)
            throws PermissionDeniedException;

    /**
     * <p>Deletes the role from the server.</p>
     * <p>This method sends an asynchronous request to Discord server.</p>
     * @param callback callback to call when the request is completed, can be null if not needed
     * @return future for obtaining the response from Discord servers
     * @throws PermissionDeniedException when the application doesn't have manage roles permission
     */
    Future<RequestResponse> delete(AsyncCallback callback) throws PermissionDeniedException;
}
