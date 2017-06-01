package cz.salmelu.discord.resources;

import java.util.List;

public interface Server {
    String getId();
    String getName();

    List<Channel> getChannels();
    Channel getChannelById(String id);
    Channel getChannelByName(String name);
}
