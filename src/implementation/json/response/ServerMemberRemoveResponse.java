package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;

public class ServerMemberRemoveResponse extends JSONMappedObject {
    private String guildId;
    private UserObject user;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public UserObject getUser() {
        return user;
    }

    public void setUser(UserObject user) {
        this.user = user;
    }
}
