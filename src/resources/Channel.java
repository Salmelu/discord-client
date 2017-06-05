package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

public interface Channel {
    String getId();
    boolean isPrivate();

    boolean canSendMessage();
    void sendMessage(String text) throws PermissionDeniedException;

    ServerChannel toServerChannel();
    PrivateChannel toPrivateChannel();
}
