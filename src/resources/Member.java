package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

import java.util.List;

public interface Member {
    String getId();
    User getUser();
    String getNickname();
    List<Role> getRoles();

    String getMention();

    Server getServer();

    PrivateChannel createPrivateChannel();

    void addRole(Role role) throws PermissionDeniedException;
    void removeRole(Role role) throws PermissionDeniedException;

    void setRoles(List<Role> roles);
    void mute(boolean mute);
    void deafen(boolean deaf);
    void moveChannel(ServerChannel newChannel);
    void changeNickname(String nickname);
}
