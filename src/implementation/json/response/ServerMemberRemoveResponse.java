package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.resources.User;

public class ServerMemberRemoveResponse extends JSONMappedObject {
    private String guildId;
    private User user;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
