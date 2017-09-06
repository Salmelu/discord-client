package cz.salmelu.discord.implementation.net.rest;

import cz.salmelu.discord.DiscordRequestException;
import cz.salmelu.discord.RequestResponse;

public class RequestResponseImpl implements RequestResponse {

    private boolean successful;
    private int statusCode;
    private String statusMessage;

    RequestResponseImpl(DiscordRequestException exception) {
        this.successful = false;
        this.statusCode = exception.getResponseCode();
        this.statusMessage = exception.getMessage();
    }

    RequestResponseImpl(int statusCode, String statusMessage) {
        this.successful = true;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public boolean isRateLimited() {
        return getStatusCode() == 429;
    }
}
