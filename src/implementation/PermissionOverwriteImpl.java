package cz.salmelu.discord.implementation;

import cz.salmelu.discord.resources.Permission;
import cz.salmelu.discord.resources.PermissionOverwrite;
import cz.salmelu.discord.resources.PermissionOverwriteType;

import java.util.EnumSet;

public class PermissionOverwriteImpl implements PermissionOverwrite {

    private final String id;
    private final PermissionOverwriteType type;
    private EnumSet<Permission> allow;
    private EnumSet<Permission> deny;

    public PermissionOverwriteImpl(PermissionOverwriteType type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public PermissionOverwriteType getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public EnumSet<Permission> getAllow() {
        return allow;
    }

    @Override
    public EnumSet<Permission> getDeny() {
        return deny;
    }

    public void addAllow(Permission permission) {
        allow.add(permission);
    }

    public void addDeny(Permission permission) {
        deny.add(permission);
    }
}
