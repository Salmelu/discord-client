package cz.salmelu.discord.implementation;

import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.resources.*;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.PresenceUpdate;
import cz.salmelu.discord.resources.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Optional;

public class Dispatcher {

    private final ClientImpl client;
    private final ModuleManager moduleManager;

    private final Logger logger;
    private final Marker marker;

    private boolean ignoreOwnMessages;
    private boolean ignoreBotMessages;

    public Dispatcher(ClientImpl client, ModuleManager manager) {
        this.client = client;
        this.moduleManager = manager;

        logger = LoggerFactory.getLogger(getClass());
        marker = MarkerFactory.getMarker("Dispatcher");
    }

    public void ignoreOwnMessages(boolean ignore) {
        this.ignoreOwnMessages = true;
    }

    public void ignoreBotMessages(boolean ignore) {
        this.ignoreBotMessages = true;
    }

    public void onReady(UserObject userObject, PrivateChannelObject[] privateChannelObjects,
                        UnavailableServerObject[] serverObjects) {
        client.setMyUser(new UserImpl(client, userObject));
        moduleManager.getInitializers().forEach(listener -> listener.onReady(client));
    }

    public void onServerCreate(ServerObject serverObject) {
        final Server server = new ServerImpl(client, serverObject);
        client.addServer(server);
        moduleManager.getInitializers().forEach(listener -> listener.onServerDetected(server));
    }

    public void onPresenceChange(PresenceUpdateObject presenceObject) {
        final PresenceUpdate update = new PresenceUpdateImpl(client, presenceObject);
        moduleManager.getUserActionListeners().forEach(listener -> listener.onPresenceChange(update));
    }

    public void onTypingStart(TypingStartObject typingStartObject) {
        moduleManager.getUserActionListeners()
                .forEach(listener -> listener.onTypingStart(typingStartObject.getUserId(),
                        typingStartObject.getChannelId(),
                        typingStartObject.getTimestamp()));
    }

    public void onMessageUpdate(MessageObject messageObject) {
        final MessageImpl message = new MessageImpl(client, messageObject);
        if(ignoreBotMessages && messageObject.getAuthor().isBot()) {
            logger.debug(marker, "Skipping a bot message.");
            return;
        }
        if(ignoreOwnMessages && messageObject.getAuthor().getId().equals(client.getMyUser().getId())) {
            logger.debug(marker, "Skipping own message.");
            return;
        }
        moduleManager.getMessageListeners().forEach(listener -> listener.onMessageUpdate(message));
    }

    public void onMessage(MessageObject messageObject) {
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
                .filter(listener -> listener.matchMessage(message)).findFirst();
        foundListener.ifPresent(listener -> listener.onMessage(message));
    }
}
