package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public User getAuthor() {
        return client.findUser(originalObject.getAuthor().getId());
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
                .map(userObject -> client.findUser(userObject.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getMentionedRoles() {
        return Arrays.stream(originalObject.getMentionRoles())
                .map(roleId -> getChannel().getServer().getRoleById(roleId))
                .collect(Collectors.toList());
    }
}
