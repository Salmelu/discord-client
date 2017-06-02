package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.GameObject;
import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.resources.PresenceStatus;

public class PresenceUpdateResponse extends JSONMappedObject {
    private UserObject user;
    private PresenceStatus status;
    private RoleObject[] roles;
    private String nick;
    private String guildId;
    private GameObject game;

    public UserObject getUser() {
        return user;
    }

    public void setUser(UserObject user) {
        this.user = user;
    }

    public PresenceStatus getStatus() {
        return status;
    }

    public void setStatus(PresenceStatus status) {
        this.status = status;
    }

    public RoleObject[] getRoles() {
        return roles;
    }

    public void setRoles(RoleObject[] roles) {
        this.roles = roles;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public GameObject getGame() {
        return game;
    }

    public void setGame(GameObject game) {
        this.game = game;
    }
}
