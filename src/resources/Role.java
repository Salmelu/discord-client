package cz.salmelu.discord.resources;

import java.util.List;
import java.util.Set;

public interface Role {

    String getId();
    String getName();
    Server getServer();

    String getMention();
    Set<Permission> getPermissions();

    void update(String name, List<Permission> permissions, int color, boolean separate, boolean mentionable);

    void delete();
}
