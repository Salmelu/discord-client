package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import org.json.JSONObject;

public class ChannelObject extends JSONMappedObject {
    private String id;
    private String guildId;
    private String name;
    private ChannelTypeObject type;
    private int position;
    private boolean isPrivate;
    private PermissionOverwriteObject[] permissionOverwrites;
    private String topic;
    private String lastMessageId;
    private int bitrate;
    private int userLimit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelTypeObject getType() {
        return type;
    }

    public void setType(ChannelTypeObject type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public PermissionOverwriteObject[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    public void setPermissionOverwrites(PermissionOverwriteObject[] overwrites) {
        this.permissionOverwrites = permissionOverwrites;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public JSONObject getModifyObject() {
        return new JSONObject()
                .put("name", name)
                .put("topic", topic)
                .put("position", position);
    }
}
