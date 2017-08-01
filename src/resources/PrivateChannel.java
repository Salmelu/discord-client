package cz.salmelu.discord.resources;

import java.util.List;

public interface PrivateChannel extends Channel {
    List<User> getUsers();
}
