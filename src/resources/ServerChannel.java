package cz.salmelu.discord.resources;

public interface ServerChannel extends Channel {
    String getName();
    Server getServer();

    String getMention();
}
