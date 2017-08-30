package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedName;

/**
 * Defined the types of the permission overwrites.
 */
public enum PermissionOverwriteType {
    /**
     * Role's permissions are overwritten
     */
    @MappedName("role")
    ROLE,
    /**
     * Member's permissions are overwritten
     */
    @MappedName("member")
    MEMBER
}
