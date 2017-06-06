package cz.salmelu.discord.resources;

public interface Reaction {
    int getCount();
    boolean isMine();
    Emoji getEmoji();
    Message getMessage();
}
