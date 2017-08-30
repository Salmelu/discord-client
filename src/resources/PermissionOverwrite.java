package cz.salmelu.discord.resources;

import java.util.EnumSet;

/**
 * <p>A permission overwrite changing some of the role or member permissions in a specific case.</p>
 */
public interface PermissionOverwrite {
    /**
     * Gets the type of the permission overwrite
     * @return overwrite type
     */
    PermissionOverwriteType getType();

    /**
     * Gets id of the overwritten entity
     * @return entity id
     */
    String getId();

    /**
     * Gets set of permissions that are explicitly allowed by this overwrite.
     * @return set of allowed permissions
     */
    EnumSet<Permission> getAllow();

    /**
     * Gets set of permissions that are explicitly denied by this overwrite.
     * @return set of denied permissions
     */
    EnumSet<Permission> getDeny();
}
