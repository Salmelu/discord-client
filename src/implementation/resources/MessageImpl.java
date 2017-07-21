package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.json.response.ReactionUpdateResponse;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MessageImpl implements Message {

    private final MessageObject originalObject;
    private Channel channel;
    private final ClientImpl client;
    private final Map<String, ReactionImpl> reactions = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(MessageImpl.class.getSimpleName());
    private final static Marker marker = MarkerFactory.getMarker("MessageImpl");

    public MessageImpl(ClientImpl client, MessageObject messageObject) {
        this.originalObject = messageObject;
        this.client = client;
        this.channel = client.getChannelById(messageObject.getChannelId());
        if(channel == null) {
            JSONObject channelObject = client.getRequester()
                    .getRequestAsObject(EndpointBuilder.create(Endpoint.CHANNEL)
                            .addElement(messageObject.getChannelId()).build());
            if(channelObject.getBoolean("is_private")) {
                PrivateChannelObject privateChannelObject =
                        client.getSerializer().deserialize(channelObject, PrivateChannelObject.class);
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
    public String getText() {
        String text = originalObject.getContent();
        for (User user : getMentionedUsers()) {
            text = text.replace(user.getMention(), "@" + user.getName());
            if(user instanceof Member) text = text.replace(((Member) user).getMention(), "@" + ((Member) user).getNickname());
        }
        for (Role role : getMentionedRoles()) {
            text = text.replace(role.getMention(), "@" + role.getName());
        }
        return text;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Collection<Reaction> getReactions() {
        return Collections.unmodifiableCollection(reactions.values());
    }

    @Override
    public User getAuthor() {
        if(originalObject.getAuthor() == null) return null;
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

    @Override
    public void addReaction(Emoji emoji) {
        final Channel channel = getChannel();
        ReactionImpl reaction = reactions.values().stream().filter(r -> r.getEmoji().equals(emoji)).findFirst().orElse(null);
        if (!channel.isPrivate()) {
            final ServerChannelImpl serverChannel = (ServerChannelImpl) channel;
            if (!serverChannel.checkPermission(Permission.READ_MESSAGE_HISTORY)) {
                throw new PermissionDeniedException("This application cannot access message history of affected channel.");
            }
            if (reaction == null) {
                if (!serverChannel.checkPermission(Permission.ADD_REACTIONS)) {
                    throw new PermissionDeniedException("This application cannot add reactions in affected channel.");
                }
            }
        }
        client.getRequester().putRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getChannel().getId()).addElement("messages").addElement(getId())
                .addElement("reactions").addElement(emoji.getUnicode()).addElement("@me").build());
        if (reaction == null) {
            reaction = new ReactionImpl(this, emoji);
            reactions.put(emoji.getName(), reaction);
        }
        else {
            reaction.increment(true);
        }
    }

    @Override
    public void removeReaction(Emoji emoji) {
        ReactionImpl reaction = reactions.values().stream().filter(r -> r.getEmoji().getName().equals(emoji)).findFirst().orElse(null);
        if(reaction == null) {
            throw new IllegalArgumentException("This message doesn't contain that emoji.");
        }
        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getChannel().getId()).addElement("messages").addElement(getId())
                .addElement("reactions").addElement(emoji.getUnicode()).addElement("@me").build());
        reaction.decrement(true);
        if(reaction.getCount() == 0) reactions.remove(reaction.getEmoji().getName());
    }

    @Override
    public List<User> getReactions(Emoji emoji) {
        JSONArray rawUsers = client.getRequester().getRequestAsArray(
                EndpointBuilder.create(Endpoint.CHANNEL).addElement(getChannel().getId())
                        .addElement("messages").addElement(getId()).addElement("reactions")
                        .addElement(emoji.getUnicode()).build());
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < rawUsers.length(); i++) {
            UserObject userObject = client.getSerializer().deserialize(rawUsers.getJSONObject(i), UserObject.class);
            User user = client.getUser(userObject.getId());
            if(user == null) {
                UserImpl newUser = new UserImpl(client, userObject);
                client.addUser(newUser);
                user = newUser;
            }
            userList.add(user);
        }
        return userList;
    }

    public ReactionImpl addReaction0(ReactionUpdateResponse reactionResponse, Emoji emoji) {
        ReactionImpl current = reactions.get(reactionResponse.getEmoji().getName());
        if(current == null) {
            ReactionImpl newReaction = new ReactionImpl(client, emoji, reactionResponse, this);
            reactions.put(newReaction.getEmoji().getName(), newReaction);
            return newReaction;
        }
        else {
            current.increment(reactionResponse.getUserId().equals(client.getMyUser().getId()));
            return current;
        }
    }

    public ReactionImpl removeReaction0(ReactionUpdateResponse reactionResponse, Emoji emoji) {
        ReactionImpl current = reactions.get(reactionResponse.getEmoji().getName());
        if(current != null) {
            current.decrement(reactionResponse.getUserId().equals(client.getMyUser().getId()));
            if(current.getCount() == 0) reactions.remove(current.getEmoji().getName());
            return current;
        }
        else {
            ReactionImpl removedReaction = new ReactionImpl(client, emoji, reactionResponse, this);
            reactions.put(removedReaction.getEmoji().getName(), removedReaction);
            return removedReaction;
        }
    }
}
