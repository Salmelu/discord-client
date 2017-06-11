package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.PermissionOverwriteImpl;

public class PermissionOverwriteBuilder {
    public static PermissionOverwriteBuilder createRoleOverwrite(Role role) {
        return new PermissionOverwriteBuilder(
                new PermissionOverwriteImpl(PermissionOverwriteType.ROLE, role.getId()));
    }

    public static PermissionOverwriteBuilder createMemberOverwrite(Member member) {
        return new PermissionOverwriteBuilder(
                new PermissionOverwriteImpl(PermissionOverwriteType.MEMBER, member.getId()));
    }

    private final PermissionOverwriteImpl perms;

    private PermissionOverwriteBuilder(PermissionOverwriteImpl perms) {
        this.perms = perms;
    }

    public PermissionOverwriteBuilder allow(Permission... permissions) {
        for (Permission permission : permissions) {
            if(perms.getAllow().contains(permission)) {
                throw new IllegalArgumentException("Duplicate allow permission " + permission.toString());
            }
            else if(perms.getDeny().contains(permission)) {
                throw new IllegalArgumentException("Cannot allow and deny the same permission " + permission.toString());
            }
            else {
                perms.addAllow(permission);
            }
        }
        return this;
    }

    public PermissionOverwriteBuilder deny(Permission... permissions) {
        for (Permission permission : permissions) {
            if(perms.getDeny().contains(permission)) {
                throw new IllegalArgumentException("Duplicate deny permission " + permission.toString());
            }
            else if(perms.getAllow().contains(permission)) {
                throw new IllegalArgumentException("Cannot allow and deny the same permission " + permission.toString());
            }
            else {
                perms.addDeny(permission);
            }
        }
        return this;
    }

    public PermissionOverwrite build() {
        return perms;
    }
}
