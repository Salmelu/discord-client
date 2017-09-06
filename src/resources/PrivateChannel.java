package cz.salmelu.discord.resources;

import java.util.List;

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
