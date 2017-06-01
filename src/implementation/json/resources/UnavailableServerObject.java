package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class UnavailableServerObject extends JSONMappedObject {
    private String id;
    private boolean unavailable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }
}
