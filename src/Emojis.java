package cz.salmelu.discord;

import cz.salmelu.discord.implementation.resources.EmojiImpl;
import cz.salmelu.discord.resources.Emoji;

import java.util.HashMap;

public class Emojis {

    public static final Emoji SMILE = new EmojiImpl(null, "smile","😄");
    public static final Emoji TONGUE = new EmojiImpl(null, "stuck_out_tongue", "\uD83D\uDE1B");

    private static final HashMap<String, Emoji> emojiByUnicode = new HashMap<>();
    private static final HashMap<String, Emoji> emojiByName = new HashMap<>();

    static {
        p(SMILE);
        p(TONGUE);
    }

    private static void p(Emoji e) {
        emojiByUnicode.put(e.getUnicode(), e);
        emojiByName.put(e.getName(), e);
    }

    public static Emoji getByUnicode(String str) {
        return emojiByUnicode.get(str);
    }

    public static Emoji getByName(String str) {
        return emojiByName.get(str);
    }

}
