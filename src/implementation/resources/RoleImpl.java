package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.RequestResponse;
import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.resources.Permission;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.Server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class RoleImpl implements Role {

    private final ServerImpl server;
    private final RoleObject originalObject;

    public RoleImpl(ServerImpl server, RoleObject object) {
        this.server = server;
        this.originalObject = object;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RoleImpl))return false;
        RoleImpl otherCast = (RoleImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public void update(RoleObject object) {
        originalObject.setName(object.getName());
        originalObject.setColor(object.getColor());
        originalObject.setPermissions(object.getPermissions());
        originalObject.setPosition(object.getPosition());
        originalObject.setManaged(object.isManaged());
        originalObject.setMentionable(object.isMentionable());
        originalObject.setPinned(object.isPinned());
    }

    @Override
    public String getId() {
        return originalObject.getId();
    }

    @Override
    public String getName() {
        return originalObject.getName();
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getMention() {
        return isMentionable() ? "<@&" + getId() + ">" : null;
    }

    @Override
    public int getColor() {
        return originalObject.getColor();
    }

    @Override
    public boolean isMentionable() {
        return originalObject.isMentionable();
    }

    @Override
    public boolean isSeparate() {
        return originalObject.isPinned();
    }

    @Override
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(Permission.getPermissions(originalObject.getPermissions()));
    }

    @Override
    public Future<RequestResponse> update(String name, List<Permission> permissions, int color, boolean separate,
                         boolean mentionable) {
        return server.updateRole(this, name, permissions, color, separate, mentionable);
    }

    @Override
    public Future<RequestResponse> delete() {
        return server.deleteRole(this);
    }
}
