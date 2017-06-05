package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.resources.UserObject;

public class ServerBanResponse extends UserObject {
    private String guildId;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}
