package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.resources.RoleImpl;

import java.util.List;

public interface Server {
    String getId();
    String getName();

    List<ServerChannel> getChannels();
    ServerChannel getChannelById(String id);
    ServerChannel getChannelByName(String name);

    RoleImpl getEveryoneRole();

    List<Role> getRoles();
    Role getRoleById(String id);
    Role getRoleByName(String name);

    List<Member> getMembers();
    Member getMemberById(String id);
    Member getMemberByNickname(String nickname);
    Member getMember(User user);
}
