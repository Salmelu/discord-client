package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class IdentifyRequest extends JSONMappedObject {
    String token;
    IdentifyRequestProperties properties;
    boolean compress;
    int largeThreshold;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public IdentifyRequestProperties getProperties() {
        return properties;
    }

    public void setProperties(IdentifyRequestProperties properties) {
        this.properties = properties;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public int getLargeThreshold() {
        return largeThreshold;
    }

    public void setLargeThreshold(int largeThreshold) {
        this.largeThreshold = largeThreshold;
    }
}
