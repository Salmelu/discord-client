package cz.salmelu.discord.implementation.net.socket;

import cz.salmelu.discord.DiscordClient;
import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.reflector.Serializer;
import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.Dispatcher;
import cz.salmelu.discord.implementation.json.request.*;
import cz.salmelu.discord.implementation.json.response.*;

import cz.salmelu.discord.implementation.net.RateLimiter;
import cz.salmelu.discord.implementation.resources.ClientImpl;
import cz.salmelu.discord.resources.Channel;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

/**
 * <p>A websocket to communicate with Discord Gateway.</p>
 * <p>Takes care of establishing connection, processing events and following Discord event flow.</p>
 */
public class DiscordWebSocket extends WebSocketAdapter {

    /** Library name presented to the gateway */
    private static final String LIB_NAME = DiscordClient.LIB_NAME;
    /** Library version presented to the gateway */
    private static final String LIB_VERSION = DiscordClient.LIB_VERSION;
    /** The amount of milliseconds after which fail counter resets */
    private static final long FAIL_RESET_PERIOD = 5 * 60 * 1000;
    /** The amount of failed tries in a short period of time that cause the gateway to shutdown */
    private static final long FAIL_LIMIT = 10;
    private final String token;

    private ClientImpl discord;
    private WebSocketClient client;
    private Session session = null;
    private HeartbeatGenerator heartbeatGenerator = new HeartbeatGenerator(this);
    private Thread heartbeatThread = null;

    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private DiscordWebSocketState state = DiscordWebSocketState.CREATED;
    private Serializer serializer;
    private Dispatcher dispatcher;
    private RateLimiter limiter;

    private long failedTries = 0;
    private long lastFailedTry = 0;
    private boolean stopping = false;

    private URI uri;
    private int sequenceNumber = -1;
    private String sessionId = null;

    public DiscordWebSocket(ClientImpl client, String token, Serializer serializer, Dispatcher dispatcher, RateLimiter limiter) {
        this.discord = client;
        this.token = token;
        this.dispatcher = dispatcher;
        this.limiter = limiter;
        this.serializer = serializer;

        this.state = DiscordWebSocketState.INITIALIZED;
    }

    /**
     * Initializes the websocket clint and tries to connect the client to Discord Websocket.
     * @param uri uri of the websocket
     */
    public void connect(URI uri) {
        if(this.state != DiscordWebSocketState.INITIALIZED) {
            logger.error("Cannot connect to websocket without initializing properly.");
            throw new Error("Cannot connect to websocket without initializing properly.");
        }
        this.uri = uri;
        this.client = new WebSocketClient(new SslContextFactory());
        this.client.setDaemon(true);
        this.client.getPolicy().setIdleTimeout(120000);
        this.client.getPolicy().setMaxBinaryMessageSize(Integer.MAX_VALUE);
        this.client.getPolicy().setMaxTextMessageSize(Integer.MAX_VALUE);
        try {
            this.client.start();
        }
        catch (Exception e) {
            logger.error("Unable to start websocket client.", e);
        }
        connect();
    }

    /**
     * Connects (or reconnects) the client back to websocket.
     */
    private synchronized void connect() {
        if(state == DiscordWebSocketState.READY ||
                state == DiscordWebSocketState.CONNECTING ||
                state == DiscordWebSocketState.RECONNECTING) {
            logger.warn("Connecting when the websocket is ready makes no sense.");
            return;
        }
        if(sessionId != null) {
            this.state = DiscordWebSocketState.RECONNECTING;
        }
        else {
            this.state = DiscordWebSocketState.CONNECTING;
        }

        try {
            if (session != null) {
                session.close();
                session = null;
            }
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setRequestURI(uri);
            client.connect(this, uri, request);
            logger.debug("Requested connection to socket at uri " + uri.toString() + ".");
        }
        catch (Exception e) {
            logger.error("Couldn't open connection to socket.", e);
        }
    }

    /**
     * <p>Disconnects the client from Discord Gateway.</p>
     * <p>This prevents automatic reconnection.</p>
     */
    public void disconnect() {
        if(state != DiscordWebSocketState.READY) {
            logger.warn("Attempting to disconnect not working websocket.");
            return;
        }
        state = DiscordWebSocketState.DISCONNECTING;
        logger.info("Disconnecting from websocket.");
        if(heartbeatThread != null) {
            logger.info("Stopping heartbeat generator.");
            heartbeatGenerator.stop();
            heartbeatThread.interrupt();
            try {
                heartbeatThread.join();
            }
            catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for heartbeat thread.");
            }
            heartbeatThread = null;
        }
        if(session != null) {
            session.close(1000, "Gracefully ending.");
            stopping = true;
        }
        logger.info("Closed connection to socket.");
    }

    /**
     * Checks current websocket client's state.
     * @return client's state
     */
    public DiscordWebSocketState getState() {
        return state;
    }

    /**
     * <p>Sends a message to the Gateway. The method is synchronized therefore it prevents
     * race conditions while sending the messages.</p>
     * @param op message op code
     * @param object sent object
     */
    public synchronized void sendMessage(int op, MappedObject object) {
        // Create the message object
        JSONObject message = new JSONObject();
        message.put("op", op);
        message.put("d", serializer.serialize(object));

        // Status updates have different rate limits, handle separately
        if(op == DiscordSocketMessage.STATUS_UPDATE) {
            long wait = limiter.checkGameUpdateLimit();
            while(wait > 0 && getState() == DiscordWebSocketState.READY) {
                logger.warn("Request to status update is being rate limited, " +
                        "waiting for " + wait + " milliseconds.");
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException ignored) {}
                wait = limiter.checkGameUpdateLimit();
            }
        }

        if(op == DiscordSocketMessage.HEARTBEAT
                || op == DiscordSocketMessage.RESUME
                || op == DiscordSocketMessage.IDENTIFY) {
            // Skip the checks
            sendMessage0(message.toString(), true);
        }
        else {
            sendMessage(message.toString());
        }
    }

    /**
     * Sends a String message to the Discord gateway.
     * @param message sent message
     */
    synchronized void sendMessage(String message) {
        if(state != DiscordWebSocketState.READY) {
            logger.warn("Cannot send messages when the connection is not ready, skipping.");
            return;
        }
        sendMessage0(message, false);
    }

    /**
     * Internally sends the message, bypassing the ready check. Needed for heartbeat.
     * @param message sent message
     * @param skip skip the rate limit check
     */
    synchronized void sendMessage0(String message, boolean skip) {
        if(session == null || !session.isOpen()) {
            logger.warn("No opened connection to socket.");
            return;
        }
        logger.debug("Sending message " + message.replace(token, "TOKEN"));

        // Block until the gateway is ready to get new request, initializing messages bypass this
        if(!skip) {
            long wait = limiter.checkGatewayLimit();
            while (wait > 0) {
                logger.warn("Request to status update is being rate limited, " +
                        "waiting for " + wait + " milliseconds.");
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wait = limiter.checkGatewayLimit();
            }
        }

        // Send the message
        try {
            session.getRemote().sendString(message);
        }
        catch (Exception e) {
            logger.warn("Sending message failed with an exception.", e);
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        logger.error("Websocket threw an error: " + cause.getMessage(), cause);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.info("Connection to discord gateway was successfully opened.");
        this.session = session;
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        this.state = DiscordWebSocketState.DISCONNECTED;

        // A mechanism to kill the client, if it's failing too often.
        // Spamming websocket too much is risking a ban.
        if(System.currentTimeMillis() - lastFailedTry > FAIL_RESET_PERIOD) {
            // more than a minute ago, reset
            failedTries = 1;
        }
        else {
            ++failedTries;
        }
        lastFailedTry = System.currentTimeMillis();
        if(failedTries >= FAIL_LIMIT) {
            // Too many fails, sorry, but we can't spam websocket that much, good bye
            this.state = DiscordWebSocketState.DEAD;
            logger.error("Connection was closed, code = " + code + ", message = " + reason);
            logger.error("This was last allowed failure in a row, aborting websocket attempts. Goodbye.");
            throw new Error("Websocket kept failing to connect, aborting.");
        }

        heartbeatGenerator.pause();
        if(code == 1000) {
            logger.info("Connection was closed gracefully, code = " + code + ", message = " + reason);
            if(!stopping) connect();
        }
        else {
            logger.warn("Connection was closed, code = " + code + ", message = " + reason);
            connect();
        }
    }

    @Override
    public void onWebSocketText(String s) {
        try {
            logger.debug("Received a message: " + s);
            JSONObject message = new JSONObject(s);
            int op = message.getInt("op");

            // Process the message depending on OP code
            switch (op) {
                case DiscordSocketMessage.DISPATCH:
                    sequenceNumber = message.getInt("s");
                    heartbeatGenerator.updateSequence(sequenceNumber);
                    dispatch(message.getJSONObject("d"), message.getString("t"));
                    break;
                case DiscordSocketMessage.HEARTBEAT:
                    heartbeatGenerator.sendHeartbeat();
                    break;
                case DiscordSocketMessage.HEARTBEAT_ACK:
                    heartbeatGenerator.heartbeatAck();
                    break;
                case DiscordSocketMessage.HELLO:
                    final HelloResponse event = serializer.deserialize(message.getJSONObject("d"), HelloResponse.class);
                    heartbeatGenerator.setInterval(event.getHeartbeatInterval());
                    heartbeatGenerator.resume(true);
                    if (state == DiscordWebSocketState.RECONNECTING) {
                        logger.debug("Resuming connection from previous state.");
                        resume();
                    }
                    else {
                        logger.debug("Sending identify request.");
                        identify();
                    }
                    break;
                case DiscordSocketMessage.RECONNECT:
                    state = DiscordWebSocketState.DISCONNECTED;
                    logger.debug("Clearing connection and trying again from scratch.");
                    connect();
                    break;
                case DiscordSocketMessage.INVALID_SESSION:
                    logger.warn("Invalid session event received.");
                    final boolean resumable = message.getBoolean("d");

                    try {
                        Thread.sleep(3500);
                    }
                    catch(InterruptedException e) {
                        logger.warn("Interrupted", e);
                    }
                    if(resumable) {
                        logger.debug("Attempting to resume connection from previous state.");
                        resume();
                    }
                    else {
                        discord.purgeData();
                        logger.debug("Sending identify request.");
                        identify();
                    }
                    break;
                default:
                    logger.warn("Invalid message type received.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int offset, int len) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new InflaterInputStream(
                                new ByteArrayInputStream(bytes, offset, len))));
        onWebSocketText(reader.lines().collect(Collectors.joining()));
    }

    /**
     * Resumes the websocket connection. This requires a valid session id and sequence number and causes
     * the gateway to resend messages the client missed.
     */
    private void resume() {
        if(state != DiscordWebSocketState.RECONNECTING && state != DiscordWebSocketState.CONNECTING) return;
        state = DiscordWebSocketState.RESUMING;

        final ResumeRequest request = new ResumeRequest();
        request.setToken(token);
        request.setSessionId(sessionId);
        request.setSeq(sequenceNumber);

        sendMessage(DiscordSocketMessage.RESUME, request);
    }

    /**
     * Requests offline members in a given server. This is needed for large servers as those do not
     * present a complete list of users on connection.
     * @param serverId id of requested server
     * @param username an optional pattern for usernames, leave empty string if not needed
     * @param limit maximum amount of users requested, leave 0 if all are needed
     */
    public void requestOfflineMembers(String serverId, String username, int limit) {
        if(state != DiscordWebSocketState.READY) return;
        JSONObject data = new JSONObject();
        data.put("guild_id", serverId);
        data.put("query", username);
        data.put("limit", limit);

        JSONObject message = new JSONObject();
        message.put("op", DiscordSocketMessage.REQUEST_GUILD_MEMBERS);
        message.put("d", data);

        sendMessage(message.toString());
    }

    /**
     * Updates user's presence status.
     * @param gameName name of currently played game
     * @param idle how long is the user idle
     */
    public void statusUpdate(String gameName, Long idle) {
        if(state != DiscordWebSocketState.READY) return;
        final GameObject game = new GameObject();
        game.setName(gameName);

        final StatusUpdateRequest request = new StatusUpdateRequest();
        request.setSince(idle);
        request.setGame(game);
        sendMessage(DiscordSocketMessage.STATUS_UPDATE, request);
    }

    /**
     * Sends the identify request to the gateway.
     */
    private void identify() {
        if(state != DiscordWebSocketState.CONNECTING) return;
        final IdentifyRequestProperties properties = new IdentifyRequestProperties();
        properties.set$device(LIB_NAME);
        properties.set$browser(LIB_NAME);

        final IdentifyRequest request = new IdentifyRequest();
        request.setToken(token);
        request.setProperties(properties);
        request.setCompress(true);
        request.setLargeThreshold(250);

        sendMessage(DiscordSocketMessage.IDENTIFY, request);
    }

    /**
     * The gateway heartbeat timed out. Close the session and attempt to reconnect.
     */
    void timeout() {
        if(state != DiscordWebSocketState.READY) return;
        logger.warn("Detected heartbeat timeout.");
        heartbeatGenerator.pause();
        session.close(4006, "Didn't receive heartbeat.");
        session = null;
    }

    /**
     * Dispatches an event for the listeners.
     * @param data received gateway data
     * @param type event type
     */
    private void dispatch(JSONObject data, String type) {
        int channelType;
        try {
            if (state == DiscordWebSocketState.DISCONNECTING) return;
            switch (type) {
                case "READY":
                    state = DiscordWebSocketState.READY;
                    dispatchReady(data);
                    break;
                case "RESUMED":
                    state = DiscordWebSocketState.READY;
                    break;
                case "CHANNEL_CREATE":
                    channelType = data.getInt("type");
                    if(channelType == Channel.ChannelType.PRIVATE || channelType == Channel.ChannelType.PRIVATE_GROUP)
                        dispatcher.onChannelCreate(serializer.deserialize(data, PrivateChannelObject.class));
                    else
                        dispatcher.onChannelCreate(serializer.deserialize(data, ChannelObject.class));
                    break;
                case "CHANNEL_UPDATE":
                    dispatcher.onChannelUpdate(serializer.deserialize(data, ChannelObject.class));
                    break;
                case "CHANNEL_DELETE":
                    channelType = data.getInt("type");
                    if(channelType == Channel.ChannelType.PRIVATE || channelType == Channel.ChannelType.PRIVATE_GROUP)
                        dispatcher.onChannelDelete(serializer.deserialize(data, PrivateChannelObject.class));
                    else
                        dispatcher.onChannelDelete(serializer.deserialize(data, ChannelObject.class));
                    break;
                case "GUILD_CREATE":
                    dispatcher.onServerCreate(serializer.deserialize(data, ServerObject.class));
                    break;
                case "GUILD_DELETE":
                    dispatcher.onServerDelete(data.getString("id"));
                    break;
                case "GUILD_UPDATE":
                    dispatcher.onServerUpdate(serializer.deserialize(data, ServerObject.class));
                    break;
                case "GUILD_BAN_ADD":
                    // Ignored
                    break;
                case "GUILD_BAN_REMOVE":
                    // Ignored
                    break;
                case "GUILD_EMOJIS_UPDATE":
                    // Ignored
                    break;
                case "GUILD_INTEGRATIONS_UPDATE":
                    // Ignored
                    break;
                case "GUILD_MEMBER_ADD":
                    dispatcher.onServerMemberAdd(serializer.deserialize(data, ServerMemberAddResponse.class));
                    break;
                case "GUILD_MEMBER_REMOVE":
                    dispatcher.onServerMemberRemove(serializer.deserialize(data, ServerMemberRemoveResponse.class));
                    break;
                case "GUILD_MEMBER_UPDATE":
                    dispatcher.onServerMemberUpdate(serializer.deserialize(data, ServerMemberUpdateResponse.class));
                    break;
                case "GUILD_MEMBER_CHUNK":
                    dispatcher.onServerMemberChunk(serializer.deserialize(data, ServerMemberChunkResponse.class));
                    break;
                case "GUILD_ROLE_CREATE":
                    dispatcher.onRoleCreate(serializer.deserialize(data, ServerRoleResponse.class));
                    break;
                case "GUILD_ROLE_UPDATE":
                    dispatcher.onRoleUpdate(serializer.deserialize(data, ServerRoleResponse.class));
                    break;
                case "GUILD_ROLE_DELETE":
                    dispatcher.onRoleDelete(serializer.deserialize(data, ServerRoleDeleteResponse.class));
                    break;
                case "CHANNEL_PINS_UPDATE":
                    dispatcher.onChannelPins(data.getString("channel_id"));
                    break;
                case "MESSAGE_CREATE":
                    dispatcher.onMessage(serializer.deserialize(data, MessageObject.class));
                    break;
                case "MESSAGE_UPDATE":
                    dispatcher.onMessageUpdate(serializer.deserialize(data, MessageObject.class));
                    break;
                case "MESSAGE_DELETE":
                    dispatcher.onMessageDelete(serializer.deserialize(data, MessageDeleteResponse.class));
                    break;
                case "MESSAGE_DELETE_BULK":
                    dispatcher.onMessageDeleteBulk(serializer.deserialize(data, MessageDeleteBulkResponse.class));
                    break;
                case "MESSAGE_REACTION_ADD":
                    dispatcher.onReactionAdd(serializer.deserialize(data, ReactionUpdateResponse.class));
                    break;
                case "MESSAGE_REACTION_REMOVE":
                    dispatcher.onReactionRemove(serializer.deserialize(data, ReactionUpdateResponse.class));
                    break;
                case "PRESENCE_UPDATE":
                    dispatcher.onPresenceChange(serializer.deserialize(data, PresenceUpdateResponse.class));
                    break;
                case "TYPING_START":
                    dispatcher.onTypingStart(serializer.deserialize(data, TypingStartResponse.class));
                    break;
                case "USER_UPDATE":
                    dispatcher.onUserUpdate(serializer.deserialize(data, UserObject.class));
                    break;
                case "VOICE_STATE_UPDATE":
                    // Ignored, we don't implement voice
                    break;
                case "VOICE_SERVER_UPDATE":
                    // Ignored, we don't implement voice
                    break;
                case "WEBHOOKS_UPDATE":
                    // Ignored
                    break;
                default:
                    logger.warn("Unrecognized event received.");
                    break;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // This one is a bit special, handle with caution
    private void dispatchReady(JSONObject data) {
        // int version = data.getInt("v");
        sessionId = data.getString("session_id");
        // We already have user object from @me at initialization phase
        // final UserObject user = UserObject.deserialize(data.getJSONObject("user"), UserObject.class);

        final List<PrivateChannelObject> privateList = new ArrayList<>();
        for (Object channel : data.getJSONArray("private_channels")) {
            privateList.add(serializer.deserialize((JSONObject) channel,
                    PrivateChannelObject.class));
        }
        final PrivateChannelObject[] privateChannels = privateList.toArray(new PrivateChannelObject[privateList.size()]);

        final List<UnavailableServerObject> serverList = new ArrayList<>();
        for (Object server : data.getJSONArray("guilds")) {
            serverList.add(serializer.deserialize((JSONObject) server,
                    UnavailableServerObject.class));
        }
        final UnavailableServerObject[] servers = serverList.toArray(new UnavailableServerObject[serverList.size()]);

        if(heartbeatThread == null) {
            logger.debug("Starting heartbeat thread.");
            heartbeatThread = new Thread(() -> heartbeatGenerator.start());
            heartbeatThread.start();
        }

        dispatcher.onReady(privateChannels, servers);
    }
}
