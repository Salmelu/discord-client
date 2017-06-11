package cz.salmelu.discord.resources;

import cz.salmelu.discord.Emoji;

public interface Reaction {
    int getCount();
    boolean isMine();
    Emoji getEmoji();
    Message getMessage();
}
