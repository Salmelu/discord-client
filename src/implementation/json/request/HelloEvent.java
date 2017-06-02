package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class HelloEvent extends JSONMappedObject {
    private int heartbeatInterval;

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
}
