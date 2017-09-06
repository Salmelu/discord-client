package cz.salmelu.discord.implementation.net.socket;

/**
 * Discord Message codes used in Discord events.
 */
final class DiscordSocketMessage {
    static final int DISPATCH = 0;
    static final int HEARTBEAT = 1;
    static final int IDENTIFY = 2;
    static final int STATUS_UPDATE = 3;
    static final int VOICE_STATE_UPDATE = 4;
    static final int VOICE_SERVER_PING = 5;
    static final int RESUME = 6;
    static final int RECONNECT = 7;
    static final int REQUEST_GUILD_MEMBERS = 8;
    static final int INVALID_SESSION = 9;
    static final int HELLO = 10;
    static final int HEARTBEAT_ACK = 11;
}
