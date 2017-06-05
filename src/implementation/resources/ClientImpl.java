package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.Dispatcher;
import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.net.*;
import cz.salmelu.discord.resources.*;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
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

        URI uri;
        try {
            uri = new URI(gateway);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        socket = new DiscordWebSocket(botToken, dispatcher, limiter);
        socket.connect(uri);
    }

    public void verifyUser() {
        try {
            JSONObject userObject = getRequester().getRequestAsObject(Endpoint.MY_USER);
            myUser = new UserImpl(this, JSONMappedObject.deserialize(userObject, UserObject.class));
        }
        catch (DiscordRequestException e) {
            if(e.getResponseCode() == 401 || e.getResponseCode() == 403) {
                throw new IllegalArgumentException("The access token used is invalid.");
            }
            else {
                throw e;
            }
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

        verifyUser();
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

    public UserImpl getUser(String id) {
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

    public synchronized void addChannel(Channel channel) {
        channelList.add(channel);
        channelsById.put(channel.getId(),channel);
    }

    public synchronized void removeChannel(Channel channel) {
        channelList.remove(channel);
        channelsById.remove(channel.getId());
        if(!channel.isPrivate()) channelsByName.remove(channel.toServerChannel().getName());
    }

    public synchronized void addChannels(List<ServerChannel> channels) {
        for(ServerChannel channel : channels) {
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
