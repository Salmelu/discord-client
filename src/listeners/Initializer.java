package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.Client;
import cz.salmelu.discord.resources.Server;

public interface Initializer {
    default void onReady(Client client) {

    }

    default void onServerDetected(Server server) {

    }

    default void onServerUpdate(Server server) {

    }

    default void onServerDelete(Server server) {

    }
}
