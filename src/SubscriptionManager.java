package cz.salmelu.discord;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;

/**
 * <p>Manages subscriptions to channel or server notifications.</p>
 *
 * <p>Subscriptions and their effects are purely user defined.
 * {@link SubscriptionManager} serves as a helper for managing subscribed users and
 * creating a list of mentions for them when needed.</p>
 *
 * <p>All methods are module based (handled by their full classname) therefore each module has
 * its own subscription lists.</p>
 *
 * <p>Example usage for a module <i>broadcast</i>:
 * <ul>
 *     <li>During initialization, call {@link #registerChannel(Channel)} on broadcast channel</li>
 *     <li>!broadcast subscribe: calls {@link #addSubscriber(Message)}</li>
 *     <li>!broadcast unsubscribe: calls {@link #removeSubscriber(Message)}</li>
 *     <li>!broadcast message Hello: uses "Hello "
 *          + {@link #getSubscribers(Channel)} to mention all subscribed members</li>
 * </ul>
 * </p>
 */
public interface SubscriptionManager {
    /**
     * <p>Registers a channel for subscriptions.
     * Server members can only subscribe in registered servers, to allow channel-based subscriptions.</p>
     * @param channel registered channel
     */
    void registerChannel(Channel channel);

    /**
     * <p>Unregisters a channel from subscriptions.</p>
     * @param channel unregistered channel
     */
    void unregisterChannel(Channel channel);

    /**
     * <p>Adds a new subscriber.</p>
     *
     * <p>The subscriber is added to the channel where the message was posted.
     * If the channel is not registered, {@link SubscriptionManager} automatically replies with an error message.</p>
     * @param message message which triggered the subscription
     */
    default void addSubscriber(Message message) {
        addSubscriber(message, "This channel does not accept subscriptions.");
    }

    /**
     * <p>Adds a new subscriber.</p>
     *
     * <p>The subscriber is added to the channel where the message was posted.
     * If the channel is not registered, {@link SubscriptionManager} automatically replies with an error message.</p>
     * @param message message which triggered the subscription
     * @param errorMessage a message used to reply if the subscription failed
     */
    void addSubscriber(Message message, String errorMessage);

    /**
     * <p>Remove a subscriber.</p>
     *
     * <p>The subscriber is removed from the channel where the message was posted.</p>
     * @param message message which triggered the subscription
     */
    default void removeSubscriber(Message message) {
        removeSubscriber(message, "This channel does not accept subscriptions.");
    }

    /**
     * <p>Remove a subscriber.</p>
     *
     * <p>The subscriber is removed from the channel where the message was posted.</p>
     * @param message message which triggered the subscription
     * @param errorMessage a message used to reply if the command failed
     */
    void removeSubscriber(Message message, String errorMessage);

    /**
     * <p>Gives a list of subscribed members.</p>
     *
     * <p>The members are returned as a list of member mentions automatically,
     * which can be immediately concatenated to a channel message.</p>
     * @param channel the channel requesting the subscription list
     * @return a list of member mentions
     */
    String getSubscribers(Channel channel);
}
