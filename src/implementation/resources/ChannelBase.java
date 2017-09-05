package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.*;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.response.ReactionUpdateResponse;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class ChannelBase implements Channel {

    protected ClientImpl client;
    protected final Map<String, MessageImpl> messageCache = new HashMap<>();

    public void cacheMessage(MessageImpl message) {
        messageCache.put(message.getId(), message);
    }

    public boolean hasCachedMessage(String id) {
        return messageCache.containsKey(id);
    }

    public void removeCachedMessage(String id) {
        if(messageCache.containsKey(id)) messageCache.remove(id);
    }

    public void clearCache() {
        messageCache.clear();
    }

    abstract public void messageArrived(Message message);

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
            JSONObject rawObject = client.getRequester().getRequestAsObject(
                    EndpointBuilder.create(Endpoint.CHANNEL).addElement(getId())
                            .addElement("messages").addElement(id).build());
            MessageObject messageObject = client.getSerializer().deserialize(rawObject, MessageObject.class);
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

    private List<Message> getMessagesCommon(HashMap<String, String> params) {
        try {
            JSONArray rawObjects = client.getRequester().getRequestAsArray(EndpointBuilder
                    .create(Endpoint.CHANNEL).addElement(getId()).addElement("messages")
                    .addParamMap(params).build());
            if(rawObjects == null) return null;
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < rawObjects.length(); i++) {
                MessageObject messageObject = client.getSerializer().deserialize(rawObjects.getJSONObject(i), MessageObject.class);
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
        final HashMap<String, String> params = new HashMap<>();
        params.put("before", messageId);
        params.put("limit", String.valueOf(limit));
        return getMessagesCommon(params);
    }

    @Override
    public List<Message> getMessagesAround(String messageId, int limit) throws PermissionDeniedException {
        if(limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        final HashMap<String, String> params = new HashMap<>();
        params.put("around", messageId);
        params.put("limit", String.valueOf(limit));
        return getMessagesCommon(params);
    }

    @Override
    public List<Message> getMessagesAfter(String messageId, int limit) throws PermissionDeniedException {
        if(limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        final HashMap<String, String> params = new HashMap<>();
        params.put("after", messageId);
        params.put("limit", String.valueOf(limit));
        return getMessagesCommon(params);
    }

    @Override
    public Future<RequestResponse> triggerTyping(AsyncCallback callback) {
        return client.getRequester().postRequestAsync(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("typing").build(), callback);
    }

    @Override
    public List<Message> getPinnedMessages() {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("pins").build();
        JSONArray array = client.getRequester().getRequestAsArray(endpoint);

        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            MessageObject messageObject = client.getSerializer().deserialize(jsonObject, MessageObject.class);
            MessageImpl message = new MessageImpl(client, messageObject);
            messages.add(message);
        }
        return messages;
    }

    @Override
    public Future<RequestResponse> pinMessage(Message message, AsyncCallback callback) {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("pins").addElement(message.getId()).build();
        return client.getRequester().putRequestAsync(endpoint, callback);
    }

    @Override
    public Future<RequestResponse> unpinMessage(Message message, AsyncCallback callback) {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("pins").addElement(message.getId()).build();
        return client.getRequester().deleteRequestAsync(endpoint, callback);
    }

    @Override
    public Future<RequestResponse> deleteChannel(AsyncCallback callback) throws PermissionDeniedException {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL).addElement(getId()).build();
        return client.getRequester().deleteRequestAsync(endpoint, callback);
    }
}
