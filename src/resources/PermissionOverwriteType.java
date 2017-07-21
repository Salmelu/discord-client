package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedName;

public enum PermissionOverwriteType {
    @MappedName("role")
    ROLE,
    @MappedName("member")
    MEMBER
}
