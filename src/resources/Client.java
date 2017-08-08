package cz.salmelu.discord.resources;

import java.util.List;

public interface Client {
    User getMyUser();
    User getUserById(String id);

    List<Server> getServers();
    Server getServerById(String id);
    Server getServerByName(String name);

    List<ServerChannel> getServerChannels();
    List<PrivateChannel> getPrivateChannels();

    void reloadPrivateChannels();

    Channel getChannelById(String id);
}
