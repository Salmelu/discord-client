package cz.salmelu.discord.listeners;

import cz.salmelu.discord.implementation.resources.MessageImpl;
import cz.salmelu.discord.resources.Message;

public interface MessageListener extends Initializer {
    default boolean matchMessage(Message message) {
        return false;
    }

    // Only if match message returned true
    default void onMessage(Message message) {

    }

    default void onMessageUpdate(Message message) {

    }

    default int getPriority() {
        return 1000;
    }
}
