package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.GameObject;

public class StatusUpdateRequest extends JSONMappedObject {
    private Long idle;
    private GameObject game;

    public GameObject getGame() {
        return game;
    }

    public void setGame(GameObject game) {
        this.game = game;
    }

    public Long getIdle() {
        return idle;
    }

    public void setIdle(Long idle) {
        this.idle = idle;
    }
}
