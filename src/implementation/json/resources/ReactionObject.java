package cz.salmelu.discord.implementation.json.resources;

public class ReactionObject {
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
