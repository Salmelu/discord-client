package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.DiscordRequestException;
import cz.salmelu.discord.implementation.Dispatcher;
import cz.salmelu.discord.implementation.json.reflector.Serializer;
import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.net.*;
import cz.salmelu.discord.implementation.net.rest.*;
import cz.salmelu.discord.implementation.net.socket.DiscordWebSocket;
import cz.salmelu.discord.implementation.net.socket.DiscordWebSocketState;
import cz.salmelu.discord.resources.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientImpl implements Client {
    private final String botToken;
    private final DiscordRequester requester;
    private final RateLimiter limiter;
    private final Serializer serializer;
    private DiscordWebSocket socket;

    private final List<Server> serverList = new ArrayList<>();
    private final Map<String, Server> serversByName = new HashMap<>();
    private final Map<String, Server> serversById = new HashMap<>();

    private final List<ServerChannel> serverChannelList = new ArrayList<>();
    private final List<PrivateChannel> privateChannelList = new ArrayList<>();
    private final Map<String, Channel> channelsById = new HashMap<>();

    private final List<User> userList = new ArrayList<>();
    private final Map<String, User> usersById = new HashMap<>();
    private User myUser = null;

    public void login(Dispatcher dispatcher) {
        final JSONObject gatewayResponse = requester.getRequestAsObject(Endpoint.GATEWAY);
        final String gateway = gatewayResponse.getString("url") + "?v=6&encoding=json";

        URI uri;
        try {
            uri = new URI(gateway);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        socket = new DiscordWebSocket(this, botToken, serializer, dispatcher, limiter);
        socket.connect(uri);
    }

    private void verifyUser() {
        try {
            JSONObject userObject = getRequester().getRequestAsObject(Endpoint.MY_USER);
            myUser = new UserImpl(this, serializer.deserialize(userObject, UserObject.class));
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

    public void purgeData() {
        // needed when the connection invalidates, all is reloaded anyway
        serverList.clear();
        serversByName.clear();
        serversById.clear();

        serverChannelList.clear();
        privateChannelList.clear();
        channelsById.clear();

        userList.clear();
        usersById.clear();
        myUser = null;
    }

    public void logout() {
        requester.stop();
        socket.disconnect();
    }

    public ClientImpl(String token) {
        this.botToken = token;
        this.limiter = new RateLimiter();
        this.serializer = new Serializer();
        this.requester = new DiscordRequester(botToken, serializer, limiter);

        verifyUser();
    }

    public DiscordWebSocket getSocket() {
        return socket;
    }

    public DiscordRequester getRequester() {
        if(socket != null && socket.getState().equals(DiscordWebSocketState.DEAD)) {
            throw new Error("Websocket died completely, killing this thread too.");
        }
        return requester;
    }

    public Serializer getSerializer() {
        return serializer;
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
            serverChannelList.remove(channel);
            channelsById.remove(channel.getId());
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
    public User getUserById(String id) {
        if(usersById.containsKey(id)) {
            return usersById.get(id);
        }
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.USER).addElement(id).build();
        final JSONObject jsonObject = getRequester().getRequestAsObject(endpoint);
        final UserObject userObject = getSerializer().deserialize(jsonObject, UserObject.class);
        final UserImpl newUser = new UserImpl(this, userObject);
        addUser(newUser);
        return newUser;
    }

    @Override
    public void updateStatus(String gameName, Long idleSince) {
        if(getSocket() == null) return;
        getSocket().statusUpdate(gameName, idleSince);
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
        channelsById.put(channel.getId(),channel);
        if(channel.isPrivate()) {
            privateChannelList.add(channel.toPrivateChannel());
        }
        else {
            final ServerChannel serverChannel = channel.toServerChannel();
            serverChannelList.add(serverChannel);

        }
    }

    public synchronized void removeChannel(Channel channel) {
        if(channel.isPrivate()) privateChannelList.remove(channel);
        else serverChannelList.remove(channel);
        channelsById.remove(channel.getId());
    }

    public synchronized void addChannels(List<ServerChannel> channels) {
        for(ServerChannel channel : channels) {
            serverChannelList.add(channel);
            channelsById.put(channel.getId(), channel);
        }
    }

    @Override
    public synchronized List<ServerChannel> getServerChannels() {
        return serverChannelList;
    }

    @Override
    public synchronized List<PrivateChannel> getPrivateChannels() {
        if(privateChannelList.isEmpty()) {
            reloadPrivateChannels();
        }
        return privateChannelList;
    }

    @Override
    public synchronized void reloadPrivateChannels() {
        privateChannelList.clear();
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.USER)
                .addElement("@me").addElement("channels").build();
        JSONArray jsonArray = getRequester().getRequestAsArray(endpoint);
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            PrivateChannelObject channelObject = getSerializer().deserialize(jsonObject, PrivateChannelObject.class);
            PrivateChannelImpl channel = new PrivateChannelImpl(this, channelObject, null);
            privateChannelList.add(channel);
            channelsById.put(channel.getId(), channel);
        }
    }

    @Override
    public synchronized Channel getChannelById(String id) {
        return channelsById.get(id);
    }

}
