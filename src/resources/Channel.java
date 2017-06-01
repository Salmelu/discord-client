package cz.salmelu.discord.resources;

public interface Channel {
    String getId();
    String getName();
    Server getServer();

    String getMention();

    boolean canSendMessage();
    void sendMessage(String text);
}
