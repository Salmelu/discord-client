package cz.salmelu.discord;

import cz.salmelu.discord.implementation.DiscordClientImpl;

/**
 * <p>Entry point of the library.</p>
 * <p>Initializes the Discord client and suspends the main thread.</p>
 */
public class DiscordClient {
    public static void main(String[] args) {
        final DiscordClientImpl client = new DiscordClientImpl();
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
