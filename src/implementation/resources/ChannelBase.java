package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.response.ReactionUpdateResponse;
import cz.salmelu.discord.implementation.net.DiscordRequestException;
import cz.salmelu.discord.implementation.net.Endpoint;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChannelBase implements Channel {

    protected ClientImpl client;
    protected final Map<String, MessageImpl> messageCache = new HashMap<>();

    public void cacheMessage(MessageImpl message) {
        messageCache.put(message.getId(), message);
    }

    public ReactionImpl addReaction(ReactionUpdateResponse reaction, Emoji emoji) {
        if(messageCache.containsKey(reaction.getMessageId())) {
            // We only care if the message is present, otherwise the user needs to pull it whole anyway
            MessageImpl message = messageCache.get(reaction.getMessageId());
            return message.addReaction0(reaction, emoji);
        }
        return ((MessageImpl) getMessage(reaction.getMessageId())).addReaction0(reaction, emoji);
    }

    public ReactionImpl removeReaction(ReactionUpdateResponse reaction, Emoji emoji) {
        if(messageCache.containsKey(reaction.getMessageId())) {
            // We only care if the message is present, otherwise the user needs to pull it whole anyway
            MessageImpl message = messageCache.get(reaction.getMessageId());
            return message.removeReaction0(reaction, emoji);
        }
        return ((MessageImpl) getMessage(reaction.getMessageId())).removeReaction0(reaction, emoji);
    }

    @Override
    public Message getMessage(String id) throws PermissionDeniedException {
        if(messageCache.containsKey(id)) {
            return messageCache.get(id);
        }
        try {
            JSONObject rawObject = client.getRequester().getRequestAsObject(Endpoint.CHANNEL + "/" + getId() + "/messages/" + id);
            MessageObject messageObject = MessageObject.deserialize(rawObject, MessageObject.class);
            final MessageImpl message = new MessageImpl(client, messageObject);
            messageCache.put(message.getId(), message);
            return message;
        }
        catch (DiscordRequestException e) {
            if(e.getResponseCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    private List<Message> getMessagesCommon(String params) {
        try {
            JSONArray rawObjects = client.getRequester().getRequestAsArray(Endpoint.CHANNEL + "/" + getId() + "/messages"
                    + "?" + params);
            if(rawObjects == null) return null;
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < rawObjects.length(); i++) {
                MessageObject messageObject = MessageObject.deserialize(rawObjects.getJSONObject(i), MessageObject.class);
                final MessageImpl message = new MessageImpl(client, messageObject);
                messages.add(message);
                messageCache.put(message.getId(), message);
            }
            return messages;
        }
        catch (DiscordRequestException e) {
            if(e.getResponseCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public List<Message> getMessagesBefore(String messageId, int limit) throws PermissionDeniedException {
        if(limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        return getMessagesCommon("before=" + messageId + "&limit=" + limit);
    }

    @Override
    public List<Message> getMessagesAround(String messageId, int limit) throws PermissionDeniedException {
        if(limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        return getMessagesCommon("around=" + messageId + "&limit=" + limit);
    }

    @Override
    public List<Message> getMessagesAfter(String messageId, int limit) throws PermissionDeniedException {
        if(limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        return getMessagesCommon("after=" + messageId + "&limit=" + limit);
    }

    @Override
    public void triggerTyping() {
        client.getRequester().postRequest(Endpoint.CHANNEL + "/" + getId() + "/typing");
    }
}
