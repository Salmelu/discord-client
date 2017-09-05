package cz.salmelu.discord.resources;

import cz.salmelu.discord.AsyncCallback;
import cz.salmelu.discord.RequestResponse;

import java.util.List;
import java.util.concurrent.Future;

/**
 * An extension of {@link Channel} to include private channel specific features.
 */
public interface PrivateChannel extends Channel {
    /**
     * Gets a list of users present in the private channel.
     * @return list of channel users
     */
    List<User> getUsers();
}
