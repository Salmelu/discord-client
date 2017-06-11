package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.NotifyManager;
import cz.salmelu.discord.implementation.events.PresenceUpdateImpl;
import cz.salmelu.discord.implementation.events.TypingStartedImpl;
import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.json.response.*;
import cz.salmelu.discord.implementation.resources.*;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.events.PresenceUpdate;
import cz.salmelu.discord.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class Dispatcher {

    private final ClientImpl client;
    private final ModuleManager moduleManager;

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class.getSimpleName());

    private boolean ignoreOwnMessages;
    private boolean ignoreBotMessages;
    private final String helpCommand;

    public Dispatcher(ClientImpl client, ModuleManager manager, String helpCommand) {
        this.client = client;
        this.moduleManager = manager;
        this.helpCommand = helpCommand;
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
                logger.warn("A module threw an exception", e);
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
            logger.warn("Notification callback threw an exception.", e);
        }
    }

    public synchronized void onReady(PrivateChannelObject[] privateChannelObjects,
                        UnavailableServerObject[] serverObjects) {
        moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onReady, client));
    }

    public void onChannelCreate(PrivateChannelObject channelObject) {
        User user = client.getUser(channelObject.getRecipient().getId());
        if(user == null) {
            UserImpl newUser = new UserImpl(client, channelObject.getRecipient());
            client.addUser(newUser);
            user = newUser;
        }
        final PrivateChannelImpl channel = new PrivateChannelImpl(client, channelObject, user);
        client.addChannel(channel);
        // TODO: fire onPrivateChannelCreate
    }

    public void onChannelCreate(ChannelObject channelObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(channelObject.getGuildId());
        if(server == null || server.isDisabled()) {
            logger.debug("Created channel for non-tracked server.");
            // Doesn't matter, we don't track this server
            return;
        }
        final ServerChannel oldChannel = server.getChannelById(channelObject.getId());
        if(oldChannel != null) {
            logger.warn("Detected old channel hanging, removing it.");
            // Something old needs to be get rid of
            server.removeChannel(oldChannel);
        }
        final ServerChannelImpl channel = new ServerChannelImpl(client, server, channelObject);
        server.addChannel(channel);
        // TODO: fire onChannelCreate
    }

    public void onChannelUpdate(ChannelObject channelObject) {
        final Channel channel = client.getChannelById(channelObject.getId());
        if(channel == null || channel.isPrivate()) {
            logger.warn("Update for channel that is tracked as private, skipping.");
            return;
        }
        ((ServerChannelImpl) channel).update(channelObject);
        // TODO: fire onChannelUpdate
    }

    public void onChannelDelete(PrivateChannelObject channelObject) {
        final Channel removed = client.getChannelById(channelObject.getId());
        client.removeChannel(removed);
        // TODO: fire onChannelPrivateDelete
    }

    public void onChannelDelete(ChannelObject channelObject) {
        final Channel removed = client.getChannelById(channelObject.getId());
        if(removed.isPrivate()) {
            logger.warn("Remove for server  channel that is tracked as private, ignoring server.");
            client.removeChannel(removed);
            return;
        }
        ((ServerImpl) removed.toServerChannel().getServer()).removeChannel(removed.toServerChannel());
        // TODO: fire onChannelDelete
    }

    public synchronized void onServerCreate(ServerObject serverObject) {
        final Server server = new ServerImpl(client, serverObject);
        client.addServer(server);
        moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onServerDetected, server));
    }

    public synchronized void onServerDelete(String id) {
        final ServerImpl server = (ServerImpl) client.getServerById(id);
        if(server != null) {
            server.disable();
            moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onServerDelete, server));
            client.clearServer(server);
        }
    }

    public synchronized void onServerUpdate(ServerObject serverObject) {
        // WARNING: deserialize is incomplete, see https://discordapp.com/developers/docs/resources/guild#guild-object
        final ServerImpl server = (ServerImpl) client.getServerById(serverObject.getId());
        if(server == null) {
            logger.warn("No server found to be updated, skipping.");
            return;
        }
        server.update(serverObject);
        // TODO: fire onServerUpdate
    }

    public void onServerMemberAdd(ServerMemberAddResponse memberObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(memberObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        if(server.getMemberById(memberObject.getUser().getId()) != null) {
            logger.debug("Member already associated with server, skipping.");
            return;
        }
        Member member = server.addMember(memberObject);
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onMemberAdd, member));
    }

    public void onServerMemberRemove(ServerMemberRemoveResponse memberObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(memberObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        if(server.getMemberById(memberObject.getUser().getId()) == null) {
            logger.debug("Member already not in server, skipping.");
            return;
        }
        final User user = client.getUser(memberObject.getUser().getId());
        server.removeMember(user);
        // TODO: fire onMemberRemove
    }

    public void onServerMemberUpdate(ServerMemberUpdateResponse memberObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(memberObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        if(server.getMemberById(memberObject.getUser().getId()) == null) {
            logger.debug("No member in server, skipping.");
            return;
        }
        server.updateMember(memberObject);
        // TODO: fire onMemberUpdate
    }

    public void onServerMemberChunk(ServerMemberChunkResponse chunkObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(chunkObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        Arrays.stream(chunkObject.getMembers()).forEach(server::addMember);
        // TODO: fire onMemberChunk
    }

    public void onRoleCreate(ServerRoleResponse roleObject) {
        ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        final Role newRole = server.addRole(roleObject.getRole());
        // TODO: fire onRoleCreate
    }

    public void onRoleUpdate(ServerRoleResponse roleObject) {
        ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        final Role role = server.getRoleById(roleObject.getRole().getId());
        if(role == null) {
            logger.warn("No server found to be updated for role, creating a new one instead.");
            final Role newRole = server.addRole(roleObject.getRole());
            // TODO: fire onRoleUpdate
        }
        else {
            ((RoleImpl) role).update(roleObject.getRole());
            // TODO: fire onRoleUpdate
        }
    }

    public void onRoleDelete(ServerRoleDeleteResponse roleObject) {
        ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        server.removeRole(roleObject.getRoleId());
        // TODO: fire onRoleDelete
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
        User user = client.getUser(updated.getId());
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
        if(message.getAuthor() != null) {
            if (ignoreBotMessages && messageObject.getAuthor().isBot()) {
                logger.debug("Skipping a bot message.");
                return;
            }
            if (ignoreOwnMessages && messageObject.getAuthor().getId().equals(client.getMyUser().getId())) {
                logger.debug("Skipping own message.");
                return;
            }
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
        ((ChannelBase) message.getChannel()).cacheMessage(message);
        if(ignoreBotMessages && messageObject.getAuthor().isBot()) {
            logger.debug("Skipping a bot message.");
            return;
        }
        if(ignoreOwnMessages && messageObject.getAuthor().getId().equals(client.getMyUser().getId())) {
            logger.debug("Skipping your own message.");
            return;
        }
        if(message.getRawText().trim().equals(helpCommand)) {
            message.getChannel().sendMessage(moduleManager.generateCommandList());
            return;
        }
        if(message.getRawText().startsWith(helpCommand)) {
            final String[] parts = message.getRawText().split(" ", 2);
            final String name = parts[1];
            final MessageListener listener = moduleManager.getMessageListener(name);
            if(listener == null) {
                message.getChannel().sendMessage("Unknown command `" + name + "`.");
            }
            else {
                message.getChannel().sendMessage(listener.getDescription());
            }
            return;
        }

        final Optional<MessageListener> foundListener = moduleManager.getMessageListeners().stream()
                .filter(listener -> {
                    try {
                        return listener.matchMessage(message);
                    } catch (Exception e) {
                        logger.warn("Matching message threw an exception.", e);
                    }
                    return false;
                })
                .findFirst();

        foundListener.ifPresent(listener -> Wrapper.wrap(listener::onMessage, message));
    }

    public void onReactionAdd(ReactionUpdateResponse reactionObject) {
        final ChannelBase channel = (ChannelBase) client.getChannelById(reactionObject.getChannelId());
        if(channel == null) {
            // Not a tracked channel, message not tracked either
            return;
        }
        Emoji emoji = Emoji.getByUnicode(reactionObject.getEmoji().getName());
        if(emoji == null) {
            logger.debug("Unknown emoji " + reactionObject.getEmoji().getName() + ", skipping.");
            return;
        }
        channel.addReaction(reactionObject, emoji);
        // TODO: fire onReactionRemove
    }

    public void onReactionRemove(ReactionUpdateResponse reactionObject) {
        final ChannelBase channel = (ChannelBase) client.getChannelById(reactionObject.getChannelId());
        if(channel == null) {
            // Not a tracked channel, message not tracked either
            return;
        }
        Emoji emoji = Emoji.getByUnicode(reactionObject.getEmoji().getName());
        if(emoji == null) {
            logger.debug("Unknown emoji " + reactionObject.getEmoji().getName() + ", skipping.");
            return;
        }
        channel.removeReaction(reactionObject, emoji);
        // TODO: fire onReactionAdd
    }
}
