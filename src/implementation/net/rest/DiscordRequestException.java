package cz.salmelu.discord.implementation.net.rest;

public class DiscordRequestException extends RuntimeException {

    private final int responseCode;

    public DiscordRequestException(String s, int responseCode) {
        super(s);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
