package cz.salmelu.discord.resources;

import com.google.gson.annotations.SerializedName;

public enum PresenceStatus {
    @SerializedName("idle")
    IDLE,
    @SerializedName("dnd")
    DND,
    @SerializedName("online")
    ONLINE,
    @SerializedName("offline")
    OFFLINE
}
