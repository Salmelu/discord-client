package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.resources.Permission;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.Server;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
        return "<@&" + getId() + ">";
    }

    @Override
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(Permission.getPermissions(originalObject.getPermissions()));
    }

    @Override
    public void update(String name, List<Permission> permissions, int color, boolean separate, boolean mentionable) {
        server.updateRole(this, name, permissions, color, separate, mentionable);
    }

    @Override
    public void delete() {
        server.deleteRole(this);
    }
}
