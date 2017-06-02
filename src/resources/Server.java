package cz.salmelu.discord.resources;

import cz.salmelu.discord.implementation.resources.RoleImpl;

import java.util.List;

public interface Server {
    String getId();
    String getName();

    List<Channel> getChannels();
    Channel getChannelById(String id);
    Channel getChannelByName(String name);

    RoleImpl getEveryoneRole();

    List<Role> getRoles();
    Role getRoleById(String id);
    Role getRoleByName(String name);
}
