package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.reflector.Serializer;
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

import java.time.OffsetDateTime;
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
            final JSONObject channelObject = client.getRequester()
                    .getRequestAsObject(EndpointBuilder.create(Endpoint.CHANNEL)
                            .addElement(messageObject.getChannelId()).build());
            final int type = channelObject.getInt("type");
            if(type == Channel.ChannelType.PRIVATE
                    || type == Channel.ChannelType.PRIVATE_GROUP) {
                PrivateChannelObject privateChannelObject =
                        client.getSerializer().deserialize(channelObject, PrivateChannelObject.class);
                UserObject[] recipients = privateChannelObject.getRecipients();
                List<User> receivers = new ArrayList<>();
                if(recipients != null) {
                    for (UserObject recipient : recipients) {
                        UserImpl receiver = client.getUser(recipient.getId());
                        if (receiver == null) {
                            receiver = new UserImpl(client, recipient);
                            client.addUser(receiver);
                        }
                        receivers.add(receiver);
                    }
                }
                final PrivateChannelImpl channel = new PrivateChannelImpl(client, privateChannelObject, receivers);
                client.addChannel(channel);
                this.channel = channel;
            }
            else {
                logger.error(marker, "Received message from not private channel, which is not stored.");
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof MessageImpl))return false;
        MessageImpl otherCast = (MessageImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode() * 101;
    }

    public void update(MessageObject update) {
        if(update.getContent() != null) originalObject.setContent(update.getContent());
        if(update.getEditedTimestamp() != null) originalObject.setEditedTimestamp(update.getEditedTimestamp());
        if(update.isMentionEveryone() != null) originalObject.setMentionEveryone(update.isMentionEveryone());
        if(update.getMentions() != null) originalObject.setMentions(update.getMentions());
        if(update.getMentionRoles() != null) originalObject.setMentionRoles(update.getMentionRoles());
        if(update.getAttachments() != null) originalObject.setAttachments(update.getAttachments());
        if(update.getEmbeds() != null) originalObject.setEmbeds(update.getEmbeds());
        if(update.getReactions() != null) originalObject.setReactions(update.getReactions());
        if(update.getNonce() != null) originalObject.setNonce(update.getNonce());
        if(update.isPinned() != null) originalObject.setPinned(update.isPinned());
        if(update.getWebhookId() != null) originalObject.setWebhookId(update.getWebhookId());
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
    public void edit(String newText) {
        if(!getAuthor().equals(client.getMyUser())) {
            throw new PermissionDeniedException("You cannot edit messages that aren't yours.");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", newText);

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL).addElement(getChannel().getId())
                .addElement("messages").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, jsonObject);
    }

    @Override
    public void delete() {
        if(!getAuthor().equals(client.getMyUser())) {
            if(!((ServerChannelImpl) channel.toServerChannel()).checkPermission(Permission.MANAGE_MESSAGES)) {
                throw new PermissionDeniedException("The application doesn't have permission to delete other messages than its.");
            }
        }

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL).addElement(getChannel().getId())
                .addElement("messages").addElement(getId()).build();
        client.getRequester().deleteRequest(endpoint);
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
    public OffsetDateTime getSentTime() {
        return originalObject.getTimestamp();
    }

    @Override
    public OffsetDateTime getEditedTime() {
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
        if(reaction != null && reaction.isMine()) {
            throw new IllegalArgumentException("This message already contains application's reaction.");
        }
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
        ReactionImpl reaction = reactions.values().stream().filter(r -> r.getEmoji().equals(emoji)).findFirst().orElse(null);
        if(reaction == null) {
            throw new IllegalArgumentException("This message doesn't contain that emoji.");
        }
        if(!reaction.isMine()) {
            throw new IllegalArgumentException("This message doesn't contain application's reaction.");
        }
        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getChannel().getId()).addElement("messages").addElement(getId())
                .addElement("reactions").addElement(emoji.getUnicode()).addElement("@me").build());
        reaction.decrement(true);
        if(reaction.getCount() == 0) reactions.remove(reaction.getEmoji().getName());
    }

    @Override
    public void removeUserReaction(Emoji emoji, User user) {
        if(user.equals(client.getMyUser())) {
            removeReaction(emoji);
            return;
        }
        if (!channel.isPrivate()) {
            final ServerChannelImpl serverChannel = (ServerChannelImpl) channel;
            if (!serverChannel.checkPermission(Permission.MANAGE_MESSAGES)) {
                throw new PermissionDeniedException("This application cannot remove other user's reactions on this server.");
            }
        }
        ReactionImpl reaction = reactions.values().stream().filter(r -> r.getEmoji().equals(emoji)).findFirst().orElse(null);
        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getChannel().getId()).addElement("messages").addElement(getId())
                .addElement("reactions").addElement(emoji.getUnicode()).addElement(user.getId()).build());
        reaction.decrement(false);
        if(reaction.getCount() == 0) reactions.remove(reaction.getEmoji().getName());
    }

    @Override
    public void removeAllReactions() {
        if (!channel.isPrivate()) {
            final ServerChannelImpl serverChannel = (ServerChannelImpl) channel;
            if (!serverChannel.checkPermission(Permission.MANAGE_MESSAGES)) {
                throw new PermissionDeniedException("This application cannot remove other user's reactions on this server.");
            }
        }

        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getChannel().getId()).addElement("messages").addElement(getId())
                .addElement("reactions").build());
        reactions.clear();
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

    @Override
    public void pin() {
        getChannel().pinMessage(this);
    }

    @Override
    public void unpin() {
        getChannel().unpinMessage(this);
    }
}
