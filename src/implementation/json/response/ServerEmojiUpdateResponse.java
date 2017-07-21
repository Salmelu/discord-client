package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.EmojiObject;

public class ServerEmojiUpdateResponse implements MappedObject {
    private String guildId;
    private EmojiObject[] emojis;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public EmojiObject[] getEmojis() {
        return emojis;
    }

    public void setEmojis(EmojiObject[] emojis) {
        this.emojis = emojis;
    }
}
