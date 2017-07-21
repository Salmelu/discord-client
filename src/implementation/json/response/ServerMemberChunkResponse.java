package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;

public class ServerMemberChunkResponse implements MappedObject {
    private String guildId;
    private ServerMemberObject[] members;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public ServerMemberObject[] getMembers() {
        return members;
    }

    public void setMembers(ServerMemberObject[] members) {
        this.members = members;
    }
}
