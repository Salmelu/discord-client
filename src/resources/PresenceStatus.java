package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedName;

public enum PresenceStatus {
    @MappedName("idle")
    IDLE,
    @MappedName("dnd")
    DND,
    @MappedName("online")
    ONLINE,
    @MappedName("offline")
    OFFLINE
}
