package cz.salmelu.discord.resources;

import java.util.List;
import java.util.Set;

public interface Server {
    String getId();
    String getName();

    void leave();

    List<ServerChannel> getChannels();

    ServerChannel getChannelById(String id);
    ServerChannel getChannelByName(String name);

    Role getEveryoneRole();
    Set<Permission> getPermissions();

    List<Role> getRoles();
    Role getRoleById(String id);
    Role getRoleByName(String name);

    void loadAllMembers();

    List<Member> getMembers();
    Member getMemberById(String id);
    Member getMemberByNickname(String nickname);
    Member getMember(User user);

    void kickMember(Member member);
    List<User> getBannedUsers();
    void banMember(Member member, int messageDays);
    void banUser(User user, int messageDays);
    void unbanUser(User user);

    void changeMyNickname(String nickname);

    void createTextChannel(String name, List<PermissionOverwrite> overwrites);
    void createVoiceChannel(String name, int bitrate, int userLimit, List<PermissionOverwrite> overwrites);

    void createRole(String name, List<Permission> permissions, int color, boolean separate, boolean mentionable);
    void updateRole(Role role, String name, List<Permission> permissions, int color, boolean separate, boolean mentionable);
    void deleteRole(Role role);

    int getPruneMembersCount(int days);
    int pruneMembers(int days);

    void deleteChannel(ServerChannel channel);
}
