package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.*;

import java.util.List;

/**
 * <p>An interface with message callbacks for modules.</p>
 *
 * <p>A module shall implement this interface when it needs to react to received messages and their edits.</p>
 */
public interface MessageListener extends Initializer {
    /**
     * <p>Tries to match the message to desired format.</p>
     * <p>If match returns true, the {@link #onMessage(Message)} will be called only on this module.</p>
     * @param message received message
     * @return true if the message should be consumed by this module
     */
    boolean matchMessage(Message message);

    // Only if match message returned true

    /**
     * <p>Called when {@link #matchMessage(Message)} returns true.</p>
     * <p>In this method, the module should process the message contents and react accordingly.</p>
     * @param message received message
     */
    void onMessage(Message message);

    /**
     * Called when a previously posted message is changed by its author.
     * @param message updated message
     */
    default void onMessageUpdate(Message message) {

    }

    /**
     * Called when some of the previously posted messages are deleted.
     * @param messages list of deleted messages
     */
    default void onMessageDelete(List<DeletedMessage> messages) {

    }

    /**
     * Called when a reaction is added to previously posted message.
     * @param reaction added reaction and its message
     * @param user the user who reacted
     */
    default void onReactionAdd(Reaction reaction, User user) {

    }

    /**
     * Called when a reaction is removed from a posted message
     * @param reaction removed reaction and its message
     * @param user the user whose reaction was removed
     */
    default void onReactionRemove(Reaction reaction, User user) {

    }

    /**
     * Called when someone pins or unpins a message in the channel.
     * This is not called when a pinned message is deleted.
     * @param channel relevant channel
     */
    default void onPinsChange(Channel channel) {

    }

    /**
     * Determines, whether the module is listed in automatically generated help.
     * @return true if the module should be enlisted in generated help
     */
    default boolean isVisibleInHelp() {
        return false;
    }

    /**
     * Gets the name of the module. Used by internal listing and generated help.
     * @return user-defined module name
     */
    String getName();

    /**
     * Gets the module's description. This description is used solely in generated help.
     * If the module is not visible in help, this does nothing.
     * @return module's description
     */
    default String getDescription() {
        return "";
    }

    /**
     * Gets module's priority. The modules with higher priority are called prior to modules with lower
     * priority. This can be used to make a module consume more generic commands if a more specific module
     * fails to match the message.
     * @return module's priority
     */
    default int getPriority() {
        return 1000;
    }
}
