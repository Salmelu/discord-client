package cz.salmelu.discord.resources;

import java.util.EnumSet;

public interface PermissionOverwrite {
    PermissionOverwriteType getType();
    String getId();
    EnumSet<Permission> getAllow();
    EnumSet<Permission> getDeny();
}
