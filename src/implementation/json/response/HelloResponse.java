package cz.salmelu.discord.implementation.json.response;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class HelloResponse implements MappedObject {
    private int heartbeatInterval;

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
}
