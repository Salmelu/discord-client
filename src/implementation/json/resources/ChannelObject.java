package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import org.json.JSONObject;

public class ChannelObject implements MappedObject {
    private String id;
    private String guildId;
    private String name;
    private Integer type;
    private Integer position;
    private PermissionOverwriteObject[] permissionOverwrites;
    private String topic;
    private String lastMessageId;
    private Integer bitrate;
    private Integer userLimit;
    private String parentId;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
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

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(Integer userLimit) {
        this.userLimit = userLimit;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public JSONObject getModifyObject() {
        return new JSONObject()
                .put("name", name)
                .put("topic", topic)
                .put("position", position)
                .put("bitrate", bitrate)
                .put("user_limit", userLimit);
    }
}
