package cz.salmelu.discord.resources;

public interface User {
    String getId();
    String getName();
    String getDiscriminator();
    String getMention();

    PrivateChannel createPrivateChannel();
}
