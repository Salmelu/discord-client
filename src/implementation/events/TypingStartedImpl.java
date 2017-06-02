package cz.salmelu.discord.implementation.events;

import cz.salmelu.discord.events.TypingStarted;
import cz.salmelu.discord.implementation.json.response.TypingStartResponse;
import cz.salmelu.discord.implementation.resources.ClientImpl;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.User;

public class TypingStartedImpl implements TypingStarted {
    private final ClientImpl client;
    private final TypingStartResponse originalObject;

    public TypingStartedImpl(ClientImpl client, TypingStartResponse object) {
        this.client = client;
        this.originalObject = object;
    }

    @Override
    public User getUser() {
        // TODO: document it can return null, if the user is unknown
        return client.findUser(originalObject.getUserId());
    }

    @Override
    public Channel getChannel() {
        return client.getChannelById(originalObject.getChannelId());
    }

    @Override
    public long getTimestamp() {
        return originalObject.getTimestamp();
    }
}
