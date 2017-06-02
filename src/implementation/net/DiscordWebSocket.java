package cz.salmelu.discord.implementation.net;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.Dispatcher;
import cz.salmelu.discord.implementation.json.request.*;
import cz.salmelu.discord.implementation.json.response.MessageDeleteBulkResponse;
import cz.salmelu.discord.implementation.json.response.MessageDeleteResponse;
import cz.salmelu.discord.implementation.json.response.PresenceUpdateResponse;
import cz.salmelu.discord.implementation.json.response.TypingStartResponse;
import org.eclipse.jetty.websocket.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

public class DiscordWebSocket implements WebSocket, WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {

    private static final String LIB_NAME = "salmelu-discord";
    private static final String LIB_VERSION = "0.0.1";
    private String token;

    private final WebSocketClientFactory factory;
    private WebSocketClient client;
    private Connection connection = null;
    private HeartbeatGenerator heartbeatGenerator;
    private Thread heartbeatThread;

    private Logger logger;
    private Marker marker;
    private DiscordWebSocketState state;
    private Dispatcher dispatcher;
    private RateLimiter limiter;

    private URI uri;
    private int sequenceNumber = -1;
    private String sessionId;

    public DiscordWebSocket(String token, Dispatcher dispatcher, RateLimiter limiter) throws Exception {
        this.token = token;
        this.dispatcher = dispatcher;
        this.limiter = limiter;

        logger = LoggerFactory.getLogger(getClass());
        marker = MarkerFactory.getMarker("WebSocket");
        state = DiscordWebSocketState.DISCONNECTED;

        try {
            factory = new WebSocketClientFactory();
            factory.start();
        }
        catch (Exception e) {
            throw new Error(e);
        }

        heartbeatGenerator = new HeartbeatGenerator(this);
    }

    public void connect(URI uri) {
        state = DiscordWebSocketState.CONNECTING;
        this.uri = uri;
        connect();
    }

    private void connect() {
        if(state == DiscordWebSocketState.READY) {
            return;
        }
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            client = factory.newWebSocketClient();
            client.setMaxBinaryMessageSize(Integer.MAX_VALUE);
            client.setMaxTextMessageSize(Integer.MAX_VALUE);
            client.setMaxIdleTime(120000);
            client.open(uri, this);

            logger.debug(marker, "Opened connection to socket.");
        }
        catch (Exception e) {
            logger.error(marker, "Couldn't open connection to socket.");
        }
    }

    public void disconnect() {
        state = DiscordWebSocketState.DISCONNECTING;
        if(heartbeatThread != null) {
            heartbeatGenerator.stop();
            heartbeatThread.interrupt();
            try {
                heartbeatThread.join();
            }
            catch (InterruptedException e) {
                logger.warn(marker, "Interrupted while waiting for heartbeat thread.");
            }
        }
        if(connection != null) {
            connection.close(420, "Gracefully ending.");
        }
        logger.debug(marker, "Closed connection to socket.");
    }

    public DiscordWebSocketState getState() {
        return state;
    }

    public synchronized void sendMessage(int op, JSONMappedObject object) {
        JSONObject message = new JSONObject();
        message.put("op", op);
        message.put("d", object.serialize());

        if(op == DiscordSocketMessage.STATUS_UPDATE) {
            long wait = limiter.checkGameUpdateLimit();
            while(wait > 0) {
                logger.warn(marker, "Request to status update is being rate limited, " +
                        "waiting for " + wait + " milliseconds.");
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException ignored) {}
                wait = limiter.checkGameUpdateLimit();
            }
        }

        sendMessage(message.toString());
    }

    public synchronized void sendMessage(String message) throws RuntimeException {
        if(connection == null || !connection.isOpen()) {
            logger.warn(marker, "No opened connection to socket.");
            return;
        }
        logger.debug(marker, "Sending message " + message.replace(token, "TOKEN"));

        long wait = limiter.checkGatewayLimit();
        while(wait > 0) {
            logger.warn(marker, "Request to status update is being rate limited, " +
                    "waiting for " + wait + " milliseconds.");
            try {
                Thread.sleep(wait);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            wait = limiter.checkGatewayLimit();
        }

        try {
            connection.sendMessage(message);
        }
        catch (IOException e) {
            logger.warn(marker, "Sending message failed with an exception.", e);
        }
    }

    @Override
    public void onOpen(WebSocket.Connection connection) {
        logger.debug(marker, "Connection was successfully opened.");
        this.connection = connection;
    }

    @Override
    public void onClose(int i, String s) {
        logger.debug(marker, "Connection was closed, code = " + i + ", message = " + s);
    }

    @Override
    public void onMessage(String s) {
        try {
            logger.debug(marker, "Received a message: " + s);
            JSONObject message = new JSONObject(s);
            int op = message.getInt("op");

            switch (op) {
                case DiscordSocketMessage.DISPATCH:
                    sequenceNumber = message.getInt("s");
                    heartbeatGenerator.updateSequence(sequenceNumber);
                    dispatch(message.getJSONObject("d"), message.getString("t"));
                    break;
                case DiscordSocketMessage.HEARTBEAT_ACK:
                    heartbeatGenerator.heartbeatReceived();
                    break;
                case DiscordSocketMessage.HELLO:
                    final HelloEvent event = JSONMappedObject.deserialize(message.getJSONObject("d"), HelloEvent.class);
                    heartbeatGenerator.setInterval(event.getHeartbeatInterval());
                    heartbeatThread = new Thread(() -> heartbeatGenerator.start());
                    heartbeatThread.start();
                    if (state == DiscordWebSocketState.RECONNECTING) resume();
                    else identify();
                    break;
                case DiscordSocketMessage.RECONNECT:
                    state = DiscordWebSocketState.RECONNECTING;
                    connect();
                    break;
                case DiscordSocketMessage.INVALID_SESSION:
                    logger.error(marker, "Invalid session event received.");
                    break;
                default:
                    logger.warn(marker, "Invalid message type received.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(byte[] bytes, int offset, int len) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new InflaterInputStream(
                                new ByteArrayInputStream(bytes, offset, len))));
        onMessage(reader.lines().collect(Collectors.joining()));
    }

    private void resume() {
        state = DiscordWebSocketState.RESUMING;

        final ResumeRequest request = new ResumeRequest();
        request.setToken(token);
        request.setSessionId(sessionId);
        request.setSeq(sequenceNumber);

        sendMessage(DiscordSocketMessage.RESUME, request);
    }

    public void requestOfflineMembers(String serverId, String username, int limit) {
        JSONObject data = new JSONObject();
        data.put("guild_id", serverId);
        data.put("query", username);
        data.put("limit", limit);

        JSONObject message = new JSONObject();
        message.put("op", DiscordSocketMessage.REQUEST_GUILD_MEMBERS);
        message.put("d", data);

        sendMessage(message.toString());
    }

    public void statusUpdate(String gameName, Long idle) {
        final GameObject game = new GameObject();
        game.setName(gameName);

        final StatusUpdateRequest request = new StatusUpdateRequest();
        request.setIdle(idle);
        request.setGame(game);
        sendMessage(DiscordSocketMessage.STATUS_UPDATE, request);
    }

    private void identify() {
        final IdentifyRequestProperties properties = new IdentifyRequestProperties();
        properties.set$browser(LIB_NAME);
        properties.set$browser(LIB_VERSION);

        final IdentifyRequest request = new IdentifyRequest();
        request.setToken(token);
        request.setProperties(properties);
        request.setCompress(true);
        request.setLargeThreshold(250);

        sendMessage(DiscordSocketMessage.IDENTIFY, request);
    }

    public void timeout() {
        state = DiscordWebSocketState.RECONNECTING;
        heartbeatGenerator.stop();
        heartbeatThread.interrupt();
        connection.close(42, "");
        connection = null;
        try {
            heartbeatThread.join();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        connect();
    }

    private synchronized void dispatch(JSONObject data, String type) {
        if(state == DiscordWebSocketState.DISCONNECTING) return;
        switch(type) {
            case "READY":
                state = DiscordWebSocketState.READY;
                logger.debug(marker, "Ready event received.");
                dispatchReady(data);
                break;
            case "RESUMED":
                logger.debug(marker, "Resumed event received.");
                break;
            case "GUILD_CREATE":
                logger.debug(marker, "Guild create event received.");
                dispatcher.onServerCreate(ServerObject.deserialize(data, ServerObject.class));
                break;
            case "GUILD_DELETE":
                logger.debug(marker, "Guild delete event received.");
                dispatcher.onServerDelete(data.getString("id"));
                break;
            case "MESSAGE_CREATE":
                logger.debug(marker, "Message create event received.");
                dispatcher.onMessage(MessageObject.deserialize(data, MessageObject.class));
                break;
            case "MESSAGE_UPDATE":
                logger.debug(marker, "Message update event received");
                dispatcher.onMessageUpdate(MessageObject.deserialize(data, MessageObject.class));
                break;
            case "MESSAGE_DELETE":
                logger.debug(marker, "Message delete event received");
                dispatcher.onMessageDelete(MessageDeleteResponse.deserialize(data, MessageDeleteResponse.class));
                break;
            case "MESSAGE_DELETE_BULK":
                logger.debug(marker, "Message delete bulk event received");
                dispatcher.onMessageDeleteBulk(MessageDeleteBulkResponse.deserialize(data, MessageDeleteBulkResponse.class));
                break;
            case "PRESENCE_UPDATE":
                logger.debug(marker, "Presence update event received");
                dispatcher.onPresenceChange(PresenceUpdateResponse.deserialize(data, PresenceUpdateResponse.class));
                break;
            case "TYPING_START":
                logger.debug(marker, "Typing start event received");
                dispatcher.onTypingStart(TypingStartResponse.deserialize(data, TypingStartResponse.class));
                break;
            case "USER_UPDATE":
                logger.debug(marker, "User update event received");
                dispatcher.onUserUpdate(UserObject.deserialize(data, UserObject.class));
            default:
                logger.warn(marker, "Unrecognized event received.");
                break;
        }
    }

    // This one is a bit special, handle with caution
    private void dispatchReady(JSONObject data) {
        int version = data.getInt("v");
        sessionId = data.getString("session_id");
        final UserObject user = UserObject.deserialize(data.getJSONObject("user"), UserObject.class);

        final List<PrivateChannelObject> privateList = new ArrayList<>();
        for (Object channel : data.getJSONArray("private_channels")) {
            privateList.add(PrivateChannelObject.deserialize(((JSONObject) channel),
                    PrivateChannelObject.class));
        }
        final PrivateChannelObject[] privateChannels = privateList.toArray(new PrivateChannelObject[privateList.size()]);

        final List<UnavailableServerObject> serverList = new ArrayList<>();
        for (Object server : data.getJSONArray("guilds")) {
            serverList.add(UnavailableServerObject.deserialize(((JSONObject) server),
                    UnavailableServerObject.class));
        }
        final UnavailableServerObject[] servers = serverList.toArray(new UnavailableServerObject[serverList.size()]);

        dispatcher.onReady(user, privateChannels, servers);
    }

}
