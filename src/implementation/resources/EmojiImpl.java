package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.resources.Emoji;

public class EmojiImpl implements Emoji {
    private final String id;
    private final String name;
    private final String unicode;

    public EmojiImpl(String id, String name, String unicode) {
        this.id = id;
        this.name = name;
        this.unicode = unicode;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnicode() {
        return unicode;
    }

    @Override
    public boolean isCustom() {
        return false;
    }
}
