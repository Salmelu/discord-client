package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.implementation.json.resources.ReactionObject;
import cz.salmelu.discord.implementation.json.response.ReactionUpdateResponse;
import cz.salmelu.discord.resources.Message;
import cz.salmelu.discord.resources.Reaction;

public class ReactionImpl implements Reaction {

    private final ReactionObject originalObject;
    private final Emoji emoji;
    private final MessageImpl message;

    public ReactionImpl(MessageImpl message, Emoji emoji) {
        this.message = message;
        this.emoji = emoji;

        originalObject = new ReactionObject();
        originalObject.setCount(1);
        originalObject.setMe(true);
    }

    public ReactionImpl(ReactionObject reactionObject, Emoji emoji, MessageImpl message) {
        this.originalObject = reactionObject;
        this.message = message;
        this.emoji = emoji;
    }

    public ReactionImpl(ClientImpl client, Emoji emoji, ReactionUpdateResponse reactionObject, MessageImpl message) {
        this.originalObject = new ReactionObject();
        this.message = message;
        this.emoji = emoji;

        originalObject.setCount(1);
        originalObject.setMe(reactionObject.getUserId().equals(client.getMyUser().getId()));
        originalObject.setEmoji(reactionObject.getEmoji());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ReactionImpl))return false;
        ReactionImpl otherCast = (ReactionImpl) other;
        return otherCast.getMessage().equals(message)
                && otherCast.getEmoji().equals(emoji);
    }

    @Override
    public int hashCode() {
        return message.hashCode() * 631 + emoji.hashCode() * 76319;
    }

    @Override
    public int getCount() {
        return originalObject.getCount();
    }

    @Override
    public boolean isMine() {
        return originalObject.isMe();
    }

    @Override
    public Emoji getEmoji() {
        return emoji;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    public void increment(boolean me) {
        originalObject.setCount(originalObject.getCount() + 1);
        if(me) originalObject.setMe(true);
    }

    public void decrement(boolean me) {
        originalObject.setCount(originalObject.getCount() - 1);
        if(me) originalObject.setMe(false);
    }
}
