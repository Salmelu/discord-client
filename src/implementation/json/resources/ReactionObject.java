package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class ReactionObject implements MappedObject {
    private int count;
    private boolean me;
    private EmojiObject emoji;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public EmojiObject getEmoji() {
        return emoji;
    }

    public void setEmoji(EmojiObject emoji) {
        this.emoji = emoji;
    }
}
