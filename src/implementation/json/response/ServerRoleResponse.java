package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.RoleObject;

public class ServerRoleResponse implements MappedObject {
    private String guildId;
    private RoleObject role;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public RoleObject getRole() {
        return role;
    }

    public void setRole(RoleObject role) {
        this.role = role;
    }
}
