package cz.salmelu.discord.implementation.net.socket;

/**
 * Possible states of Discord Websocket.
 */
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
