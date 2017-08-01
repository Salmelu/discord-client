package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class PrivateChannelObject implements MappedObject {
    private String id;
    private boolean isPrivate;
    private UserObject[] recipients;
    private String lastMessageId;

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public UserObject[] getRecipients() {
        return recipients;
    }

    public void setRecipients(UserObject[] recipients) {
        this.recipients = recipients;
    }
}
