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
    public String getId() {
        return id;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
