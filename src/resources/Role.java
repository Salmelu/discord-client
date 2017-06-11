package cz.salmelu.discord.resources;

import java.util.Set;

public interface Role {

    String getId();
    String getName();
    String getMention();
    Set<Permission> getPermissions();
}
