package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;

public class ServerMemberAddResponse extends ServerMemberObject {
    private String guildId;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}
