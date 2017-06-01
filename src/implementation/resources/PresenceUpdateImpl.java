package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.PresenceUpdateObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.resources.*;

public class PresenceUpdateImpl implements PresenceUpdate {

    private final PresenceUpdateObject originalObject;
    private final ClientImpl client;

    public PresenceUpdateImpl(ClientImpl client, PresenceUpdateObject object) {
        this.client = client;
        this.originalObject = object;
    }

    @Override
    public User getUser() {
        final UserObject user = originalObject.getUser();
        if(user == null) return null;
        return new UserImpl(client, user);
    }

    @Override
    public PresenceStatus getStatus() {
        return originalObject.getStatus();
    }

    @Override
    public String getNickname() {
        return originalObject.getNick();
    }

    @Override
    public String getServerId() {
        return originalObject.getGuildId();
    }

    @Override
    public Game getGame() {
        return null;
    }
}
