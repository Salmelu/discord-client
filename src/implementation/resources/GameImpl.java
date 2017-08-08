package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.GameObject;
import cz.salmelu.discord.resources.Game;

public class GameImpl implements Game {
    private final GameObject originalObject;

    public GameImpl(GameObject object) {
        this.originalObject = object;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof GameImpl))return false;
        GameImpl otherCast = (GameImpl) other;
        return otherCast.getName().equals(getName())
                && otherCast.isStreaming() == isStreaming()
                && otherCast.getURL().equals(getURL());
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 37 * getURL().hashCode() * (isStreaming() ? 1 : 2);
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
