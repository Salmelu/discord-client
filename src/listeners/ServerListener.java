package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.Server;

public interface ServerListener {
    default void onServerDelete(Server server) {

    }
}
