package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.resources.Role;

public class RoleImpl implements Role {

    private final ServerImpl server;
    private final RoleObject originalObject;

    public RoleImpl(ServerImpl server, RoleObject object) {
        this.server = server;
        this.originalObject = object;
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
    public String getMention() {
        return "<@&" + getId() + ">";
    }

    public long getPermissions() {
        return originalObject.getPermissions();
    }
}
