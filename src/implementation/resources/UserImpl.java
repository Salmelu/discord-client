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

    public void update(UserObject updatedObject) {
        this.originalObject.setAvatarHash(updatedObject.getAvatarHash());
        this.originalObject.setBot(updatedObject.isBot());
        this.originalObject.setDiscriminator(updatedObject.getDiscriminator());
        this.originalObject.setEmail(updatedObject.getEmail());
        this.originalObject.setMfaEnabled(updatedObject.isMfaEnabled());
        this.originalObject.setUsername(updatedObject.getUsername());
        this.originalObject.setVerified(updatedObject.isVerified());
    }

    @Override
    public String getId() {
        return originalObject.getId();
    }

    @Override
    public String getMention() {
        return "<@" + getId() + ">";
    }
}
