package cz.salmelu.discord.listeners;

import cz.salmelu.discord.events.PresenceUpdate;
import cz.salmelu.discord.events.TypingStarted;
import cz.salmelu.discord.resources.PrivateChannel;
import cz.salmelu.discord.resources.User;

public interface UserActionListener {
    default void onTypingStart(TypingStarted typingEvent) {

    }

    default void onPresenceChange(PresenceUpdate presenceEvent) {

    }

    default void onUserUpdate(User updatedUser) {

    }

    default void onChannelOpen(PrivateChannel channel) {

    }

    default void onChannelClose(PrivateChannel channel) {

    }
}
