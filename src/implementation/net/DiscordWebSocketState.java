package cz.salmelu.discord.implementation.net;

public enum DiscordWebSocketState {
    CREATED,
    INITIALIZED,
    DISCONNECTED,
    DEAD,
    CONNECTING,
    READY,
    RECONNECTING,
    DISCONNECTING,
    RESUMING
}
