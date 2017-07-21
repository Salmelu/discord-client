package cz.salmelu.discord.implementation.net.socket;

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
