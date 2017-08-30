package cz.salmelu.discord.resources;

import java.util.EnumSet;

/**
 * <p>A permission granted by server owner to server members.</p>
 */
public enum Permission {
    CREATE_INSTANT_INVITE(0x00000001),
    KICK_MEMBERS(0x00000002),
    BAN_MEMBERS(0x00000004),
    ADMINISTRATOR(0x00000008),
    MANAGE_CHANNELS(0x00000010),
    MANAGE_GUILD(0x00000020),
    ADD_REACTIONS(0x00000040),
    READ_MESSAGES(0x00000400),
    SEND_MESSAGES(0x00000800),
    SEND_TTS_MESSAGES(0x00001000),
    MANAGE_MESSAGES(0x00002000),
    EMBED_LINKS(0x00004000),
    ATTACH_FILES(0x00008000),
    READ_MESSAGE_HISTORY(0x00010000),
    MENTION_EVERYONE(0x00020000),
    USE_EXTERNAL_EMOJIS(0x00040000),
    VOICE_CONNECT(0x00100000),
    VOICE_SPEAK(0x00200000),
    VOICE_MUTE(0x00400000),
    VOICE_DEAFEN(0x00800000),
    VOICE_MOVE(0x01000000),
    USE_VOICE_ACTIVITY(0x02000000),
    CHANGE_NICKNAME(0x04000000),
    MANAGE_NICKNAMES(0x08000000),
    MANAGE_ROLES(0x10000000),
    MANAGE_WEBHOOKS(0x20000000),
    MANAGE_EMOJIS(0x40000000);

    /** Internal bit value. */
    private final long value;

    Permission(long value) {
        this.value = value;
    }

    /**
     * Converts permission into it's bit field representation.
     * @return number with correct bit set
     */
    public long getValue() {
        return value;
    }

    /**
     * Converts a value into a set of permissions.
     * @param value value with permission bits set
     * @return set of permissions
     */
    public static EnumSet<Permission> getPermissions(long value) {
        EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
        for (Permission permission : Permission.values()) {
            if((permission.getValue() & value) > 0) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * Converts a set of permissions into one number with correct bits set.
     * @param permissions set of permissions
     * @return converted value
     */
    public static long convertToValue(EnumSet<Permission> permissions) {
        long value = 0;
        for (Permission permission : permissions) {
            value |= permission.getValue();
        }
        return value;
    }
}