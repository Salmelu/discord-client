package cz.salmelu.discord.implementation.net;

public final class DiscordSocketMessage {
    public static final int DISPATCH = 0;
    public static final int HEARTBEAT = 1;
    public static final int IDENTIFY = 2;
    public static final int STATUS_UPDATE = 3;
    public static final int VOICE_STATE_UPDATE = 4;
    public static final int VOICE_SERVER_PING = 5;
    public static final int RESUME = 6;
    public static final int RECONNECT = 7;
    public static final int REQUEST_GUILD_MEMBERS = 8;
    public static final int INVALID_SESSION = 9;
    public static final int HELLO = 10;
    public static final int HEARTBEAT_ACK = 11;
}
