package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.GameObject;

public class StatusUpdateRequest implements MappedObject {
    private Long since;
    private GameObject game;
    private String status;
    private boolean afk;

    public GameObject getGame() {
        return game;
    }

    public void setGame(GameObject game) {
        this.game = game;
    }

    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }
}
