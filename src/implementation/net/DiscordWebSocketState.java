package cz.salmelu.discord.implementation.net;

public enum DiscordWebSocketState {
    CREATED,
    INITIALIZED,
    DISCONNECTED,
    CONNECTING,
    READY,
    RECONNECTING,
    DISCONNECTING,
    RESUMING
}
