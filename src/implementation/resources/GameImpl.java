package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.GameObject;
import cz.salmelu.discord.resources.Game;

public class GameImpl implements Game {
    private final GameObject originalObject;

    public GameImpl(GameObject object) {
        this.originalObject = object;
    }

    @Override
    public String getName() {
        return originalObject.getName();
    }

    @Override
    public boolean isStreaming() {
        return originalObject.getType() == 1;
    }

    @Override
    public String getURL() {
        return originalObject.getUrl();
    }
}
