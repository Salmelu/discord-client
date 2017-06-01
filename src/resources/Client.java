package cz.salmelu.discord.resources;

import java.util.List;

public interface Client {
    User getMyUser();

    List<Server> getServers();
    Server getServerById(String id);
    Server getServerByName(String name);

    List<Channel> getChannels();
    Channel getChannelById(String id);
    Channel getChannelByName(String name);
}
