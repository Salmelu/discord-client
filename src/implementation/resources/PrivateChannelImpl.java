package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.net.Endpoint;
import cz.salmelu.discord.resources.*;

public class PrivateChannelImpl implements PrivateChannel {

    private final String id;
    private final PrivateChannelObject originalObject;
    private final ClientImpl client;
    private final User user;

    public PrivateChannelImpl(ClientImpl client, PrivateChannelObject channelObject, User user) {
        this.id = channelObject.getId();
        this.originalObject = channelObject;
        this.client = client;
        this.user = user;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof PrivateChannelImpl))return false;
        PrivateChannelImpl otherCast = (PrivateChannelImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public boolean canSendMessage() {
        return true;
    }

    @Override
    public void sendMessage(String text) {
        MessageObject messageObject = new MessageObject();
        messageObject.setContent(text);
        client.getRequester().postRequest(Endpoint.CHANNEL + "/" + id + "/messages", messageObject);
    }

    @Override
    public ServerChannel toServerChannel() {
        return null;
    }

    @Override
    public PrivateChannel toPrivateChannel() {
        return this;
    }

    @Override
    public User getUser() {
        return user;
    }
}
