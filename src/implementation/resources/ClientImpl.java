package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.Dispatcher;
import cz.salmelu.discord.implementation.net.DiscordHttpRequester;
import cz.salmelu.discord.implementation.net.Endpoint;
import cz.salmelu.discord.implementation.net.DiscordWebSocket;
import cz.salmelu.discord.implementation.net.RateLimiter;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Client;
import cz.salmelu.discord.resources.Server;
import cz.salmelu.discord.resources.User;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientImpl implements Client {
    private final String botToken;
    private final DiscordHttpRequester requester;
    private final RateLimiter limiter;
    private DiscordWebSocket socket;

    private final List<Server> serverList = new ArrayList<>();
    private final Map<String, Server> serversByName = new HashMap<>();
    private final Map<String, Server> serversById = new HashMap<>();

    private final List<Channel> channelList = new ArrayList<>();
    private final Map<String, Channel> channelsByName = new HashMap<>();
    private final Map<String, Channel> channelsById = new HashMap<>();

    private final List<User> userList = new ArrayList<>();
    private final Map<String, User> usersById = new HashMap<>();
    private User myUser = null;

    public void login(Dispatcher dispatcher) {
        final JSONObject gatewayResponse = requester.getRequestAsObject(Endpoint.GATEWAY);
        final String gateway = gatewayResponse.getString("url") + "?v=5&encoding=json";

        try {
            socket = new DiscordWebSocket(botToken, dispatcher, limiter);
            socket.connect(new URI(gateway));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        requester.stop();
        socket.disconnect();
    }

    public ClientImpl(String token) {
        this.botToken = token;
        this.limiter = new RateLimiter();
        this.requester = new DiscordHttpRequester(botToken, limiter);
    }

    public DiscordWebSocket getSocket() {
        return socket;
    }

    public DiscordHttpRequester getRequester() {
        return requester;
    }

    public synchronized void addServer(Server server) {
        serverList.add(server);
        serversByName.put(server.getName(), server);
        serversById.put(server.getId(), server);
    }

    public synchronized void clearServer(ServerImpl server) {
        serverList.remove(server);
        serversByName.remove(server.getName());
        serversById.remove(server.getId());

        server.getChannels().forEach(channel -> {
            channelList.remove(channel);
            channelsById.remove(channel.getId());
            channelsByName.remove(channel.getName());
        });
    }

    public void setMyUser(UserImpl user) {
        this.myUser = user;
        addUser(user);
    }

    public void addUser(UserImpl user) {
        userList.add(user);
        usersById.put(user.getId(), user);
    }

    public UserImpl findUser(String id) {
        return (UserImpl) usersById.get(id);
    }

    @Override
    public User getMyUser() {
        return myUser;
    }

    @Override
    public synchronized List<Server> getServers() {
        return serverList;
    }

    @Override
    public synchronized Server getServerById(String id) {
        return serversById.get(id);
    }

    @Override
    public synchronized Server getServerByName(String name) {
        return serversByName.get(name);
    }

    public synchronized void addChannels(List<Channel> channels) {
        for(Channel channel : channels) {
            channelList.add(channel);
            channelsByName.put(channel.getName(), channel);
            channelsById.put(channel.getId(), channel);
        }
    }

    @Override
    public synchronized List<Channel> getChannels() {
        return channelList;
    }

    @Override
    public synchronized Channel getChannelById(String id) {
        return channelsById.get(id);
    }

    @Override
    public synchronized Channel getChannelByName(String name) {
        return channelsByName.get(name);
    }

}
