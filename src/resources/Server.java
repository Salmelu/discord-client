package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.resources.RoleImpl;

import java.util.List;
import java.util.Set;

public interface Server {
    String getId();
    String getName();

    List<ServerChannel> getChannels();
    ServerChannel getChannelById(String id);
    ServerChannel getChannelByName(String name);

    RoleImpl getEveryoneRole();
    Set<Permission> getPermissions();

    List<Role> getRoles();
    Role getRoleById(String id);
    Role getRoleByName(String name);

    List<Member> getMembers();
    Member getMemberById(String id);
    Member getMemberByNickname(String nickname);
    Member getMember(User user);

    void createTextChannel(String name, List<PermissionOverwrite> overwrites);
    void createVoiceChannel(String name, int bitrate, int userLimit, List<PermissionOverwrite> overwrites);
}
