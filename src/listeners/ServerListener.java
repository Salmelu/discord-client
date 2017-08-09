package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.Member;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.ServerChannel;
import cz.salmelu.discord.resources.User;

import java.util.List;

public interface ServerListener {

    default void onChannelCreate(ServerChannel channel) {

    }

    default void onChannelUpdate(ServerChannel channel) {

    }

    default void onChannelDelete(ServerChannel channel) {

    }

    default void onMemberAdd(Member member) {

    }

    default void onMemberUpdate(Member member) {

    }

    default void onMemberRemove(User user) {

    }

    default void onMemberChunk(List<Member> members) {

    }

    default void onRoleCreate(Role role) {

    }

    default void onRoleUpdate(Role role) {

    }

    default void onRoleDelete(Role role) {

    }
}
