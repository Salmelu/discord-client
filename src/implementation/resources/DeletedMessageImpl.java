package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.DeletedMessage;

public class DeletedMessageImpl implements DeletedMessage {

    private final Channel channel;
    private final String id;
    private final ClientImpl client;

    public DeletedMessageImpl(ClientImpl client, String id, String channelId) {
        this.id = id;
        this.client = client;
        this.channel = client.getChannelById(channelId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof DeletedMessageImpl))return false;
        DeletedMessageImpl otherCast = (DeletedMessageImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
