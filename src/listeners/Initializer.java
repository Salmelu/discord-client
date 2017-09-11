package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.Client;
import cz.salmelu.discord.resources.Server;

/**
 * <p>An interface with server initialization callbacks for modules.</p>
 *
 * <p>A module shall implement this interface when it needs to be notified about server connections.</p>
 */
public interface Initializer {
    /**
     * Called when the client is successfully connected to gateway and ready to work.
     * @param client instance of running client
     */
    default void onReady(Client client) {

    }

    /**
     * <p>Called when a new server is fully revealed to the client by the gateway.</p>
     * @param server fully initialized server instance
     */
    default void onServerDetected(Server server) {

    }

    /**
     * <p>Called when some of the server parameters were changed.</p>
     * @param server updated server instance
     */
    default void onServerUpdate(Server server) {

    }

    /**
     * <p>Called when the client has lost access to a server.</p>
     * <p>This happens when either the server became unavailable, or the client was removed from the server.</p>
     * @param server instance of removed server, set to disabled state
     */
    default void onServerDelete(Server server) {

    }
}
