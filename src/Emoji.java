package cz.salmelu.discord;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * <p>An emoji usable in Discord's messages and reactions.</p>
 * <p>Each emoji has a name and a custom unicode sequence. The application can use the name
 * to distinguish various emoji more easily and to look up by the name.</p>
 */
public enum Emoji {
    JOY("joy", "\uD83D\uDE02"),
    SMILEY("smiley", "\uD83D\uDE03"),
    SMILE("smile", "\uD83D\uDE04"),
    SWEAT_SMILE("sweat_smile", "\uD83D\uDE05"),
    WINK("wink", "\uD83D\uDE09"),
    TONGUE("stuck_out_tongue", "\uD83D\uDE1B"),
    THUMB_UP("thumb_up", "\uD83D\uDC4D"),
    THUMB_DOWN("thumb_up", "\uD83D\uDC4E"),
    ZERO("zero", "\u0030\u20E3"),
    ONE("one", "\u0031\u20E3"),
    TWO("two", "\u0032\u20E3"),
    THREE("three", "\u0033\u20E3"),
    FOUR("four", "\u0034\u20E3"),
    FIVE("five", "\u0035\u20E3"),
    SIX("six", "\u0036\u20E3"),
    SEVEN("seven", "\u0037\u20E3"),
    EIGHT("eight", "\u0038\u20E3"),
    NINE("nine", "\u0039\u20E3"),
    WHITE_CHECK_MARK("white_check_mark", "\u2705"),
    X("x", "\u274C");

    /** emoji name */
    private String name;
    /** emoji unicode string */
    private String unicode;

    /** lookup table by unicode */
    private static HashMap<String, Emoji> emojiByUnicode = new HashMap<>();
    /** lookup table by name */
    private static HashMap<String, Emoji> emojiByName = new HashMap<>();

    static {
        for(Emoji e : EnumSet.allOf(Emoji.class)) {
            emojiByUnicode.put(e.getUnicode(), e);
            emojiByName.put(e.getName(), e);
        }
    }

    Emoji(String name, String unicode) {
        this.name = name;
        this.unicode = unicode;
    }

    /**
     * Gets the name of the emoji. The name is the same as the name in Discord clients.
     * @return emoji name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unicode string of the emoji. Use this when constructing messages.
     * @return emoji string
     */
    public String getUnicode() {
        return unicode;
    }

    /**
     * Find an emoji instance given the unicode string.
     * @param str unicode string
     * @return found emoji or null
     */
    public static Emoji getByUnicode(String str) {
        return emojiByUnicode.get(str);
    }

    /**
     * Find an emoji instance given its name.
     * @param str name string
     * @return found emoji or null
     */
    public static Emoji getByName(String str) {
        return emojiByName.get(str);
    }
}
