package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.PermissionOverwriteImpl;

/**
 * A helpful builder for creating {@link PermissionOverwrite} instances.
 */
public class PermissionOverwriteBuilder {

    /**
     * Creates a new role overwrite.
     * @param role overwritten role
     * @return builder instance
     */
    public static PermissionOverwriteBuilder createRoleOverwrite(Role role) {
        return new PermissionOverwriteBuilder(
                new PermissionOverwriteImpl(PermissionOverwriteType.ROLE, role.getId()));
    }

    /**
     * Creates a new member overwrite.
     * @param member overwritten member
     * @return builder instance
     */
    public static PermissionOverwriteBuilder createMemberOverwrite(Member member) {
        return new PermissionOverwriteBuilder(
                new PermissionOverwriteImpl(PermissionOverwriteType.MEMBER, member.getId()));
    }

    /** Overwrite being created */
    private final PermissionOverwriteImpl perms;

    /**
     * Creates a new builder.
     * @param perms starting instance of overwrites
     */
    private PermissionOverwriteBuilder(PermissionOverwriteImpl perms) {
        this.perms = perms;
    }

    /**
     * <p>Adds allow permissions to the overwrite.</p>
     * <p>This gives the affected role or members new permissions for the specific case.</p>
     * @param permissions added permissions
     * @return instance of the builder
     * @throws IllegalArgumentException when the overwrite already contains one of the permissions
     */
    public PermissionOverwriteBuilder allow(Permission... permissions) throws IllegalArgumentException {
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

    /**
     * <p>Adds deny permissions to the overwrite.</p>
     * <p>This removes some of the permissions from the affected role or members for the specific case.</p>
     * @param permissions removed permissions
     * @return instance of the builder
     * @throws IllegalArgumentException when the overwrite already contains one of the permissions
     */
    public PermissionOverwriteBuilder deny(Permission... permissions) throws IllegalArgumentException {
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

    /**
     * Finalizes the overwrite and returns a finished overwrite instance.
     * @return an overwrite instance
     */
    public PermissionOverwrite build() {
        return perms;
    }
}
