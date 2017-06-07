package cz.salmelu.discord;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;

public interface SubscriptionManager {
    <T> void registerChannel(T object, Channel channel);
    <T> void unregisterChannel(T object, Channel channel);
    default <T> void addSubscriber(T object, Message message) {
        addSubscriber(object, message, "This channel does not accept subscriptions.");
    }
    <T> void addSubscriber(T object, Message message, String errorMessage);
    default <T> void removeSubscriber(T object, Message message) {
        removeSubscriber(object, message, "This channel does not accept subscriptions.");
    }
    <T> void removeSubscriber(T object, Message message, String errorMessage);
    <T> String getSubscribers(T object, Channel channel);
}
