package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.EmojiObject;

public class ReactionUpdateResponse implements MappedObject {
    private String userId;
    private String messageId;
    private String channelId;
    private EmojiObject emoji;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public EmojiObject getEmoji() {
        return emoji;
    }

    public void setEmoji(EmojiObject emoji) {
        this.emoji = emoji;
    }
}