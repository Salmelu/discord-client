package cz.salmelu.discord.resources;

public interface PresenceUpdate {
    User getUser();
    PresenceStatus getStatus();
    String getNickname();
    String getServerId();
    Game getGame();
}
