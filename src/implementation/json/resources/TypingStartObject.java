package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class TypingStartObject extends JSONMappedObject {

    private String channelId;
    private String userId;
    private long timestamp;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
