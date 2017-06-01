package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.RoleObject;

public class RoleImpl {

    private final ServerImpl server;
    private final RoleObject originalObject;

    public RoleImpl(ServerImpl server, RoleObject object) {
        this.server = server;
        this.originalObject = object;
    }

    public String getId() {
        return originalObject.getId();
    }

    public String getName() {
        return originalObject.getName();
    }

    public long getPermissions() {
        return originalObject.getPermissions();
    }
}
