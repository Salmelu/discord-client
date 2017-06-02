package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.DeletedMessage;
import cz.salmelu.discord.resources.Message;

import java.util.List;

public interface MessageListener extends Initializer {
    default boolean matchMessage(Message message) {
        return false;
    }

    // Only if match message returned true
    default void onMessage(Message message) {

    }

    default void onMessageUpdate(Message message) {

    }

    default void onMessageDelete(List<DeletedMessage> messages) {

    }

    default int getPriority() {
        return 1000;
    }
}
