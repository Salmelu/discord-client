package cz.salmelu.discord.resources;

public interface Message {
    String getId();
    String getRawText();
    Channel getChannel();
}
