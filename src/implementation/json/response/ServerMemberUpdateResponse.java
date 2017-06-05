package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;

public class ServerMemberUpdateResponse extends JSONMappedObject {
    private String guildId;
    private String[] roles;
    private UserObject user;
    private String nick;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public UserObject getUser() {
        return user;
    }

    public void setUser(UserObject user) {
        this.user = user;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
