package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.DeletedMessage;
import cz.salmelu.discord.resources.Message;
import cz.salmelu.discord.resources.Reaction;
import cz.salmelu.discord.resources.User;

import java.util.List;

public interface MessageListener extends Initializer {
    boolean matchMessage(Message message);

    // Only if match message returned true
    void onMessage(Message message);

    default void onMessageUpdate(Message message) {

    }

    default void onMessageDelete(List<DeletedMessage> messages) {

    }

    default void onReactionAdd(Reaction reaction, User user) {

    }

    default void onReactionRemove(Reaction reaction, User user) {

    }

    default boolean isVisibleInHelp() {
        return false;
    }

    String getName();

    default String getDescription() {
        return "";
    }

    default int getPriority() {
        return 1000;
    }
}
