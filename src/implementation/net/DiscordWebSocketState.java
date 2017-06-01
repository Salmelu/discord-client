package cz.salmelu.discord.implementation.net;

public enum DiscordWebSocketState {
    DISCONNECTED,
    CONNECTING,
    READY,
    RECONNECTING,
    DISCONNECTING,
    RESUMING;
}
