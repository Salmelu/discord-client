package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;

public class MessageImpl implements Message {

    private final MessageObject originalObject;
    private final Channel channel;
    private final ClientImpl client;

    public MessageImpl(ClientImpl client, MessageObject messageObject) {
        this.originalObject = messageObject;
        this.client = client;
        this.channel = client.getChannelById(messageObject.getChannelId());
    }

    @Override
    public String getId() {
        return originalObject.getId();
    }

    @Override
    public String getRawText() {
        return originalObject.getContent();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
