package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.resources.PermissionOverwriteType;

public class PermissionOverwriteObject implements MappedObject {
    private String id;
    private PermissionOverwriteType type;
    private long allow;
    private long deny;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PermissionOverwriteType getType() {
        return type;
    }

    public void setType(PermissionOverwriteType type) {
        this.type = type;
    }

    public long getAllow() {
        return allow;
    }

    public void setAllow(long allow) {
        this.allow = allow;
    }

    public long getDeny() {
        return deny;
    }

    public void setDeny(long deny) {
        this.deny = deny;
    }
}
