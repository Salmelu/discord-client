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

    void addRole(Role role) throws PermissionDeniedException;
    void removeRole(Role role) throws PermissionDeniedException;
}
