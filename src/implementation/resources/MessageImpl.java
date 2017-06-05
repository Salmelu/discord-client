package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.net.Endpoint;
import cz.salmelu.discord.resources.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageImpl implements Message {

    private final MessageObject originalObject;
    private Channel channel;
    private final ClientImpl client;

    private final static Logger logger = LoggerFactory.getLogger(MessageImpl.class.getSimpleName());
    private final static Marker marker = MarkerFactory.getMarker("MessageImpl");

    public MessageImpl(ClientImpl client, MessageObject messageObject) {
        this.originalObject = messageObject;
        this.client = client;
        this.channel = client.getChannelById(messageObject.getChannelId());
        if(channel == null) {
            JSONObject channelObject = client.getRequester()
                    .getRequestAsObject(Endpoint.CHANNEL + "/" + messageObject.getChannelId());
            if(channelObject.getBoolean("is_private")) {
                PrivateChannelObject privateChannelObject =
                        PrivateChannelObject.deserialize(channelObject, PrivateChannelObject.class);
                UserImpl receiver = client.getUser(privateChannelObject.getRecipient().getId());
                if(receiver == null) {
                    receiver = new UserImpl(client, privateChannelObject.getRecipient());
                    client.addUser(receiver);
                }
                PrivateChannelImpl channel = new PrivateChannelImpl(client, privateChannelObject, receiver);
            }
            else {
                logger.error(marker, "Received message from not private channel, which is not stored.");
            }
        }
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

    @Override
    public User getAuthor() {
        return client.getUser(originalObject.getAuthor().getId());
    }

    @Override
    public LocalDateTime getSentTime() {
        return originalObject.getTimestamp();
    }

    @Override
    public LocalDateTime getEditedTime() {
        return originalObject.getEditedTimestamp();
    }

    @Override
    public boolean isTTS() {
        return originalObject.isTts();
    }

    @Override
    public boolean isMentionAtEveryone() {
        return originalObject.isMentionEveryone();
    }

    @Override
    public List<User> getMentionedUsers() {
        return Arrays.stream(originalObject.getMentions())
                .map(userObject -> client.getUser(userObject.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getMentionedRoles() {
        if(getChannel().isPrivate()) return new ArrayList<>();
        return Arrays.stream(originalObject.getMentionRoles())
                .map(roleId -> getChannel().toServerChannel().getServer().getRoleById(roleId))
                .collect(Collectors.toList());
    }

    @Override
    public void reply(String reply) {
        getChannel().sendMessage(reply);
    }
}
