package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

public interface Channel {
    String getId();
    String getName();
    Server getServer();

    String getMention();

    boolean canSendMessage();
    void sendMessage(String text) throws PermissionDeniedException;
}
