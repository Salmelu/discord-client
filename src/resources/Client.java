package cz.salmelu.discord.resources;

import java.util.List;

public interface Client {
    /**
     * Returns a {@link User} object representing application's user
     * @return application's user
     */
    User getMyUser();

    /**
     * <p>Gets a user with a specific id.</p>
     * <p>If the user is not cached, blocks until the call to Discord server is finished.</p>
     * @param id requested user's id
     * @return a user object or null, if there is no such user
     */
    User getUserById(String id);

    /**
     * <p>Changes application's presence.</p>
     * <p>Blocks until the call finishes.</p>
     * @param gameName name of the game this application is <i>playing</i>.
     * @param idleSince how long the application is idle, use <i>null</i> if not idle
     */
    void updateStatus(String gameName, Long idleSince);

    /**
     * <p>Gets all known servers this application is connected to.</p>
     * @return a list of server instances
     */
    List<Server> getServers();

    /**
     * <p>Gets a specific server given by id.</p>
     * <p><i>Attention:</i> Since Discord sends server information asynchronously,
     * the server may not be present even after client logins. If this call returns <i>null</i>
     * and you are sure the server exists, wait a while and retry.</p>
     * @param id id of requested server
     * @return server instance or null, if the client doesn't know such server
     */
    Server getServerById(String id);

    /**
     * <p>Gets a server by its name.</p>
     * <p>If there are multiple such servers, if returns any of them.</p>
     * <p><i>Attention:</i> Since Discord sends server information asynchronously,
     * the server may not be present even after client logins. If this call returns <i>null</i>
     * and you are sure the server exists, wait a while and retry.</p>
     * @param name name of the requested server
     * @return any known server instance with a given name or null, if there is no such server
     */
    Server getServerByName(String name);

    /**
     * <p>Get all known server channels.</p>
     * @return list of server channels
     */
    List<ServerChannel> getServerChannels();

    /**
     * <p>Get all opened private channels.</p>
     * <p>This call returns only those private channels, that were used in this
     * instance of application and weren't closed.</p>
     * <p>If there is an opened private channel the application needs to use, call {@link #reloadPrivateChannels()}.</p>
     * @return list of private channels
     */
    List<PrivateChannel> getPrivateChannels();

    /**
     * <p>Reloads opened private channels from Discord server.</p>
     * <p>This call blocks until the private channels are retrieved from Discord servers.</p>
     */
    void reloadPrivateChannels();

    /**
     * <p>Gets a specific channel by id.</p>
     * <p>If the channel is not loaded on client, the application doesn't send a request to Discord.
     * If such behavior is needed, see {@link #reloadPrivateChannels()}.</p>
     * @param id id of the requested channel
     * @return a requested channel instance, or null if no channel with this id is accessible
     */
    Channel getChannelById(String id);
}
