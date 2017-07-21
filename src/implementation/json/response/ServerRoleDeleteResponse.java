package cz.salmelu.discord.implementation.json.response;


import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class ServerRoleDeleteResponse implements MappedObject {
    private String guildId;
    private String roleId;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
