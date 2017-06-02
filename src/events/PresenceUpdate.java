package cz.salmelu.discord.events;

import cz.salmelu.discord.resources.Client;
import cz.salmelu.discord.resources.Game;
import cz.salmelu.discord.resources.PresenceStatus;
import cz.salmelu.discord.resources.User;

public interface PresenceUpdate {
    User getUser();
    PresenceStatus getStatus();
    String getNickname();
    String getServerId();
    Game getGame();
}
