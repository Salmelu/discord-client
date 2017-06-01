package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.PresenceUpdate;

public interface UserActionListener {
    default void onTypingStart(String userId, String channelId, long timestamp) {

    }

    default void onPresenceChange(PresenceUpdate presenceUpdate) {

    }
}
