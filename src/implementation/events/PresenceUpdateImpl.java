package cz.salmelu.discord.implementation.events;

import cz.salmelu.discord.implementation.json.response.PresenceUpdateResponse;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.resources.ClientImpl;
import cz.salmelu.discord.implementation.resources.GameImpl;
import cz.salmelu.discord.implementation.resources.UserImpl;
import cz.salmelu.discord.resources.*;
import cz.salmelu.discord.events.PresenceUpdate;

public class PresenceUpdateImpl implements PresenceUpdate {

    private final PresenceUpdateResponse originalObject;
    private final ClientImpl client;
    private final GameImpl game;

    public PresenceUpdateImpl(ClientImpl client, PresenceUpdateResponse object) {
        this.client = client;
        this.originalObject = object;
        this.game = new GameImpl(object.getGame());
    }

    @Override
    public User getUser() {
        final UserObject userObject = originalObject.getUser();
        if(userObject == null) return null;
        User user = client.findUser(userObject.getId());
        if(user == null) {
            final UserImpl newUser = new UserImpl(client, userObject);
            client.addUser(newUser);
            user = newUser;
        }
        return user;
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
        return game;
    }
}
