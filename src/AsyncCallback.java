package cz.salmelu.discord;

import cz.salmelu.discord.resources.Role;

import java.util.List;

/**
 * <p>A callback for asynchronous Discord server calls.</p>
 * <p>You can use these to retry failed requests or react to successes.</p>
 * <p><i>Notice:</i> Most of the requests fire an event on success (e.g.
 * {@link cz.salmelu.discord.resources.Server#createRole(String, List, int, boolean, boolean, AsyncCallback)} triggers
 * {@link cz.salmelu.discord.listeners.ServerListener#onRoleCreate(Role)} event), therefore you usually don't have to
 * implement {@link #completed(RequestResponse)}.</p>
 */
public interface AsyncCallback {
    /**
     * Called when the request was completed. This does not guarantee successful completion.
     * Check the response data to determine, if there was a problem with the request.
     * @param response server response
     */
    void completed(RequestResponse response);

    /**
     * Called when the request failed.
     * @param e exception describing the failure
     */
    void failed(DiscordRequestException e);

    /**
     * Called when the request was cancelled.
     */
    void cancelled();
}
