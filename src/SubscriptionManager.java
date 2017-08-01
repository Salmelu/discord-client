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
 *     <li>During initialization, call {@link #registerChannel(Object, Channel)} on broadcast channel</li>
 *     <li>!broadcast subscribe: triggers {@link #addSubscriber(Object, Message)}</li>
 *     <li>!broadcast unsubscribe: triggers {@link #removeSubscriber(Object, Message)}</li>
 *     <li>!broadcast message Hello: uses "Hello "
 *          + {@link #getSubscribers(Object, Channel)} to mention all subscribed members</li>
 * </ul>
 * </p>
 */
public interface SubscriptionManager {
    /**
     * <p>Registers a channel for subscriptions.
     * Server members can only subscribe in registered servers, to allow channel-based subscriptions.</p>
     * @param object a module registering the channel
     * @param channel registered channel
     */
    <T> void registerChannel(T object, Channel channel);

    /**
     * <p>Unregisters a channel from subscriptions.</p>
     * @param object a module unregistering the channel
     * @param channel unregistered channel
     */
    <T> void unregisterChannel(T object, Channel channel);

    /**
     * <p>Adds a new subscriber.</p>
     *
     * <p>The subscriber is added to the channel where the message was posted.
     * If the channel is not registered, {@link SubscriptionManager} automatically replies with an error message.</p>
     * @param object a module which caught the subscribing command
     * @param message message which triggered the subscription
     */
    default <T> void addSubscriber(T object, Message message) {
        addSubscriber(object, message, "This channel does not accept subscriptions.");
    }

    /**
     * <p>Adds a new subscriber.</p>
     *
     * <p>The subscriber is added to the channel where the message was posted.
     * If the channel is not registered, {@link SubscriptionManager} automatically replies with an error message.</p>
     * @param object a module which caught the subscribing command
     * @param message message which triggered the subscription
     * @param errorMessage a message used to reply if the subscription failed
     */
    <T> void addSubscriber(T object, Message message, String errorMessage);

    /**
     * <p>Remove a subscriber.</p>
     *
     * <p>The subscriber is removed from the channel where the message was posted.</p>
     * @param object a module which caught the command
     * @param message message which triggered the subscription
     */
    default <T> void removeSubscriber(T object, Message message) {
        removeSubscriber(object, message, "This channel does not accept subscriptions.");
    }

    /**
     * <p>Remove a subscriber.</p>
     *
     * <p>The subscriber is removed from the channel where the message was posted.</p>
     * @param object a module which caught the command
     * @param message message which triggered the subscription
     * @param errorMessage a message used to reply if the command failed
     */
    <T> void removeSubscriber(T object, Message message, String errorMessage);

    /**
     * <p>Gives a list of subscribed members.</p>
     *
     * <p>The members are returned as a list of member mentions automatically,
     * which can be immediately concatenated to a channel message.</p>
     * @param object a module using this method
     * @param channel the channel requesting the subscription list
     * @return a list of member mentions
     */
    <T> String getSubscribers(T object, Channel channel);
}
