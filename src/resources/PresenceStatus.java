package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedName;

/**
 * Various user presence statuses.
 */
public enum PresenceStatus {
    /**
     * The user is online but not currently present at their Discord client.
     */
    @MappedName("idle")
    IDLE,
    /**
     * The user doesn't want to be disturbed.
     */
    @MappedName("dnd")
    DND,
    /**
     * The user is currently online.
     */
    @MappedName("online")
    ONLINE,
    /**
     * The user is currently offline.
     */
    @MappedName("offline")
    OFFLINE
}
