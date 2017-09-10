package cz.salmelu.discord;

import cz.salmelu.discord.implementation.DiscordModulesImpl;

/**
 * <p>Entry point of the library.</p>
 * <p>Initializes the Discord client and suspends the main thread.</p>
 */
public class DiscordModules {
    public static final String LIB_NAME = "discord-modules";
    public static final String LIB_VERSION = "1.0.0";
    public static final String LIB_URL = "salmelu.cz";

    public static void main(String[] args) {
        final DiscordModulesImpl client = new cz.salmelu.discord.implementation.DiscordModulesImpl();
        if(!client.isStarted()) {
            System.err.println("Client couldn't start because of some issues. Check the log.");
            System.exit(1);
        }
        try {
            Thread.sleep(Integer.MAX_VALUE);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
