package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class MessageDeleteResponse implements MappedObject {
    private String id;
    private String channelId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
