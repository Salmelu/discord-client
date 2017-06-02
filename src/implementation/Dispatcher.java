package cz.salmelu.discord.implementation;

import cz.salmelu.discord.NotifyManager;
import cz.salmelu.discord.implementation.events.PresenceUpdateImpl;
import cz.salmelu.discord.implementation.events.TypingStartedImpl;
import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.json.response.MessageDeleteBulkResponse;
import cz.salmelu.discord.implementation.json.response.MessageDeleteResponse;
import cz.salmelu.discord.implementation.json.response.PresenceUpdateResponse;
import cz.salmelu.discord.implementation.json.response.TypingStartResponse;
import cz.salmelu.discord.implementation.resources.*;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.events.PresenceUpdate;
import cz.salmelu.discord.resources.DeletedMessage;
import cz.salmelu.discord.resources.Server;
import cz.salmelu.discord.resources.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class Dispatcher {

    private final ClientImpl client;
    private final ModuleManager moduleManager;

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private static final Marker marker = MarkerFactory.getMarker("Dispatcher");

    private boolean ignoreOwnMessages;
    private boolean ignoreBotMessages;

    public Dispatcher(ClientImpl client, ModuleManager manager) {
        this.client = client;
        this.moduleManager = manager;
    }

    /**
     * Wrapped so we can call all the modules and know they don't disrupt each other by throwing exceptions.
     *
     * Each exception is logged.
     *
     * @param <T> type of the passed object
     */
    private interface Wrapper<T> {
        void dispatch(T a);

        static <T> void wrap(Wrapper<T> method, T a) {
            try {
                method.dispatch(a);
            }
            catch (Exception e) {
                logger.warn(marker, "A module threw an exception", e);
            }
        }
    }

    public void ignoreOwnMessages(boolean ignore) {
        this.ignoreOwnMessages = true;
    }

    public void ignoreBotMessages(boolean ignore) {
        this.ignoreBotMessages = true;
    }

    public synchronized void fireNotification(NotifyManager.Callback callback, Object o) {
        try {
            callback.call(o);
        }
        catch (Exception e) {
            logger.warn(marker, "Notification callback threw an exception.", e);
        }
    }

    public synchronized void onReady(UserObject userObject, PrivateChannelObject[] privateChannelObjects,
                        UnavailableServerObject[] serverObjects) {
        client.setMyUser(new UserImpl(client, userObject));
        moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onReady, client));
    }

    public synchronized void onServerCreate(ServerObject serverObject) {
        final Server server = new ServerImpl(client, serverObject);
        client.addServer(server);
        moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onServerDetected, server));
    }

    public synchronized void onServerDelete(String id) {
        final Server server = client.getServerById(id);
        if(server != null) {
            ServerImpl serverTyped = (ServerImpl) server;
            serverTyped.disable();
            moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onServerDelete, server));
            client.clearServer(serverTyped);
        }
    }

    public synchronized void onPresenceChange(PresenceUpdateResponse presenceObject) {
        final PresenceUpdate update = new PresenceUpdateImpl(client, presenceObject);
        moduleManager.getUserActionListeners().forEach(listener -> Wrapper.wrap(listener::onPresenceChange, update));
    }

    public synchronized void onTypingStart(TypingStartResponse typingStartResponse) {
        final TypingStartedImpl event = new TypingStartedImpl(client, typingStartResponse);
        moduleManager.getUserActionListeners()
                .forEach(listener -> Wrapper.wrap(listener::onTypingStart, event));
    }

    public synchronized void onUserUpdate(UserObject updated) {
        User user = client.findUser(updated.getId());
        if(user == null) {
            final UserImpl newUser = new UserImpl(client, updated);
            client.addUser(newUser);
            user = newUser;
        }
        else {
            ((UserImpl) user).update(updated);
        }
        final User finalUser = user;
        moduleManager.getUserActionListeners().forEach(listener -> Wrapper.wrap(listener::onUserUpdate, finalUser));
    }

    public synchronized void onMessageUpdate(MessageObject messageObject) {
        final MessageImpl message = new MessageImpl(client, messageObject);
        if(ignoreBotMessages && messageObject.getAuthor().isBot()) {
            logger.debug(marker, "Skipping a bot message.");
            return;
        }
        if(ignoreOwnMessages && messageObject.getAuthor().getId().equals(client.getMyUser().getId())) {
            logger.debug(marker, "Skipping own message.");
            return;
        }
        moduleManager.getMessageListeners().forEach(listener -> Wrapper.wrap(listener::onMessageUpdate, message));
    }

    public synchronized void onMessageDelete(MessageDeleteResponse messageObject) {
        final MessageDeleteBulkResponse converted = new MessageDeleteBulkResponse();
        converted.setChannelId(messageObject.getChannelId());
        converted.setIds(new String[] {messageObject.getId()});
        onMessageDeleteBulk(converted);
    }

    public synchronized void onMessageDeleteBulk(MessageDeleteBulkResponse messagesObject) {
        final List<DeletedMessage> messageList =
                stream(messagesObject.getIds())
                .map(id -> new DeletedMessageImpl(client, id, messagesObject.getChannelId()))
                .collect(Collectors.toList());
        moduleManager.getMessageListeners().forEach(listener -> Wrapper.wrap(listener::onMessageDelete, messageList));
    }

    public synchronized void onMessage(MessageObject messageObject) {
        final MessageImpl message = new MessageImpl(client, messageObject);
        if(ignoreBotMessages && messageObject.getAuthor().isBot()) {
            logger.debug(marker, "Skipping a bot message.");
            return;
        }
        if(ignoreOwnMessages && messageObject.getAuthor().getId().equals(client.getMyUser().getId())) {
            logger.debug(marker, "Skipping own message.");
            return;
        }

        final Optional<MessageListener> foundListener = moduleManager.getMessageListeners().stream()
                .filter(listener -> {
                    try {
                        return listener.matchMessage(message);
                    } catch (Exception e) {
                        logger.warn(marker, "Matching message threw an exception.", e);
                    }
                    return false;
                })
                .findFirst();

        foundListener.ifPresent(listener -> Wrapper.wrap(listener::onMessage, message));
    }
}
