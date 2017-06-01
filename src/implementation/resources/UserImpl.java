package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.resources.Client;

public class UserImpl implements cz.salmelu.discord.resources.User {

    private final UserObject originalObject;
    private final Client client;

    public UserImpl(Client client, UserObject userObject) {
        this.originalObject = userObject;
        this.client = client;
    }

    @Override
    public String getId() {
        return originalObject.getId();
    }
}
