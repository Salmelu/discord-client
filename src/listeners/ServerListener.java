package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.*;

import java.util.List;

/**
 * <p>An interface with server-related callbacks (such as channel changes, role changes, member changes, etc.)
 * for modules.</p>
 *
 * <p>A module shall implement this interface when it needs to react to server changes.</p>
 */
public interface ServerListener {

    /**
     * Called when a new channel was created on the server.
     * @param channel instance representing the created channel
     */
    default void onChannelCreate(ServerChannel channel) {

    }

    /**
     * Called when an existing channel was changed.
     * @param channel instance of the updated channel
     */
    default void onChannelUpdate(ServerChannel channel) {

    }

    /**
     * Called when a channel was deleted from the server.
     * @param channel instance of deleted channel
     */
    default void onChannelDelete(ServerChannel channel) {

    }

    /**
     * Called when a new member was added to the server.
     * @param member the added member
     */
    default void onMemberAdd(Member member) {

    }

    /**
     * Called when a server member is updated.
     * @param member updated member
     */
    default void onMemberUpdate(Member member) {

    }

    /**
     * Called when a server member is removed from the server.
     * @param user removed user
     */
    default void onMemberRemove(User user) {

    }

    /**
     * Called when the client receives a chunk of members.
     * This is received as a reaction to {@link Server#loadAllMembers()}.
     * @param members list of received members
     */
    default void onMemberChunk(List<Member> members) {

    }

    /**
     * Called when a new role is added to the server.
     * @param role created role
     */
    default void onRoleCreate(Role role) {

    }

    /**
     * Called when a role is updated on the server.
     * @param role updated role
     */
    default void onRoleUpdate(Role role) {

    }

    /**
     * Called when a role is deleted from the server.
     * @param role deleted role
     */
    default void onRoleDelete(Role role) {

    }
}
