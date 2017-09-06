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

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

/**
 * Takes care of processing events, updating Discord data structures and passing the events to the listener modules.
 */
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
                logger.warn("A module threw an exception.", e);
            }
        }
    }

    /**
     * Wrapped so we can call all the modules and know they don't disrupt each other by throwing exceptions.
     *
     * Each exception is logged.
     *
     * @param <T> type of the first passed object
     * @param <U> type of the second passed object
     */
    private interface Wrapper2<T, U> {
        void dispatch(T a, U b);

        static <T, U> void wrap(Wrapper2<T, U> method, T a, U b) {
            try {
                method.dispatch(a, b);
            }
            catch (Exception e) {
                logger.warn("A module threw an exception.", e);
            }
        }
    }

    void ignoreOwnMessages(boolean ignore) {
        this.ignoreOwnMessages = ignore;
    }

    void ignoreBotMessages(boolean ignore) {
        this.ignoreBotMessages = ignore;
    }

    synchronized void fireNotification(NotifyManager.Callback callback, Object o) {
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

    public synchronized void onChannelCreate(PrivateChannelObject channelObject) {
        final List<User> users = new ArrayList<>();
        final UserObject[] userObjects = channelObject.getRecipients();
        if(userObjects != null) {
            for(UserObject userObject : userObjects) {
                final User user = client.getUser(userObject.getId());
                if (user == null) {
                    UserImpl newUser = new UserImpl(client, userObject);
                    client.addUser(newUser);
                    users.add(newUser);
                }
                else {
                    users.add(user);
                }
            }
        }
        final PrivateChannelImpl channel = new PrivateChannelImpl(client, channelObject, users);
        client.addChannel(channel);
        moduleManager.getUserActionListeners().forEach(
                listener -> Wrapper.wrap(listener::onChannelOpen, channel));
    }

    public synchronized void onChannelCreate(ChannelObject channelObject) {
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
        moduleManager.getServerListeners().forEach(
                listener -> Wrapper.wrap(listener::onChannelCreate, channel));
    }

    public synchronized void onChannelUpdate(ChannelObject channelObject) {
        final Channel channel = client.getChannelById(channelObject.getId());
        if(channel == null || channel.isPrivate()) {
            logger.warn("Update for channel that is tracked as private, skipping.");
            return;
        }
        ((ServerChannelImpl) channel).update(channelObject);
        moduleManager.getServerListeners().forEach(
                listener -> Wrapper.wrap(listener::onChannelUpdate, channel.toServerChannel()));
    }

    public synchronized void onChannelDelete(PrivateChannelObject channelObject) {
        final Channel removed = client.getChannelById(channelObject.getId());
        client.removeChannel(removed);
        moduleManager.getUserActionListeners().forEach(
                listener -> Wrapper.wrap(listener::onChannelClose, removed.toPrivateChannel()));
    }

    public synchronized void onChannelDelete(ChannelObject channelObject) {
        final Channel removed = client.getChannelById(channelObject.getId());
        if(removed.isPrivate()) {
            client.removeChannel(removed);
            return;
        }
        ((ServerImpl) removed.toServerChannel().getServer()).removeChannel(removed.toServerChannel());
        moduleManager.getServerListeners().forEach(
                listener -> Wrapper.wrap(listener::onChannelDelete, removed.toServerChannel()));
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
        // NOTICE: deserialize is incomplete, see https://discordapp.com/developers/docs/resources/guild#guild-object
        final ServerImpl server = (ServerImpl) client.getServerById(serverObject.getId());
        if(server == null) {
            logger.warn("No server found to be updated, skipping.");
            return;
        }
        server.update(serverObject);
        moduleManager.getInitializers().forEach(listener -> Wrapper.wrap(listener::onServerUpdate, server));
    }

    public synchronized void onChannelPins(String channelId) {
        final Channel channel = client.getChannelById(channelId);
        if(channel == null) return;
        moduleManager.getMessageListeners().forEach(listener -> Wrapper.wrap(listener::onPinsChange, channel));
    }

    public synchronized void onServerMemberAdd(ServerMemberAddResponse memberObject) {
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

    public synchronized void onServerMemberRemove(ServerMemberRemoveResponse memberObject) {
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
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onMemberRemove, user));
    }

    public synchronized void onServerMemberUpdate(ServerMemberUpdateResponse memberObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(memberObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        if(server.getMemberById(memberObject.getUser().getId()) == null) {
            logger.debug("No member in server, skipping.");
            return;
        }
        final Member updated = server.updateMember(memberObject);
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onMemberUpdate, updated));
    }

    public synchronized void onServerMemberChunk(ServerMemberChunkResponse chunkObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(chunkObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated with new member, skipping.");
            return;
        }
        final List<Member> newMembers = new ArrayList<>();
        Arrays.stream(chunkObject.getMembers()).forEach(memberObject -> {
            Member member = server.addMember(memberObject);
            newMembers.add(member);
        });
        final List<Member> immutable = Collections.unmodifiableList(newMembers);
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onMemberChunk, immutable));
    }

    public synchronized void onRoleCreate(ServerRoleResponse roleObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        final Role newRole = server.addRole(roleObject.getRole());
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onRoleCreate, newRole));
    }

    public synchronized void onRoleUpdate(ServerRoleResponse roleObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        final Role role = server.getRoleById(roleObject.getRole().getId());
        if(role == null) {
            logger.warn("No server found to be updated for role, creating a new one instead.");
            final Role newRole = server.addRole(roleObject.getRole());
            moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onRoleUpdate, newRole));
        }
        else {
            ((RoleImpl) role).update(roleObject.getRole());
            moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onRoleUpdate, role));
        }
    }

    public synchronized void onRoleDelete(ServerRoleDeleteResponse roleObject) {
        final ServerImpl server = (ServerImpl) client.getServerById(roleObject.getGuildId());
        if(server == null) {
            logger.warn("No server found to be updated for role, skipping.");
            return;
        }
        Role role = server.removeRole(roleObject.getRoleId());
        moduleManager.getServerListeners().forEach(listener -> Wrapper.wrap(listener::onRoleDelete, role));
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
        final String id = messageObject.getId();
        final String channelId = messageObject.getChannelId();
        final ChannelBase channel = (ChannelBase) client.getChannelById(channelId);
        if(channel == null) {
            logger.warn("Unknown channel has received message update event, skipping.");
            return;
        }
        MessageImpl message;
        if(channel.hasCachedMessage(id)) {
            message = (MessageImpl) channel.getMessage(id);
            message.update(messageObject);
        }
        else {
            message = (MessageImpl) channel.getMessage(id);
        }
        if(message.getAuthor() != null) {
            if (ignoreBotMessages && messageObject.getAuthor().isBot()) {
                logger.debug("Skipping a bot message.");
                return;
            }
            if (ignoreOwnMessages && messageObject.getAuthor().equals(client.getMyUser())) {
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
        for (DeletedMessage deletedMessage : messageList) {
            ChannelBase channel = (ChannelBase) deletedMessage.getChannel();
            if(channel != null) channel.removeCachedMessage(deletedMessage.getId());
        }
        moduleManager.getMessageListeners().forEach(listener -> Wrapper.wrap(listener::onMessageDelete, messageList));
    }

    public synchronized void onMessage(MessageObject messageObject) {
        final MessageImpl message = new MessageImpl(client, messageObject);
        final ChannelBase channelBase = (ChannelBase) message.getChannel();
        channelBase.cacheMessage(message);
        channelBase.messageArrived(message);

        if(ignoreBotMessages && messageObject.getAuthor().isBot()) {
            logger.debug("Skipping a bot message.");
            return;
        }
        if(ignoreOwnMessages && messageObject.getAuthor().equals(client.getMyUser())) {
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

    public synchronized void onReactionAdd(ReactionUpdateResponse reactionObject) {
        final ChannelBase channel = (ChannelBase) client.getChannelById(reactionObject.getChannelId());
        if(channel == null) {
            // Not a tracked channel, message not tracked either
            return;
        }
        final Emoji emoji = Emoji.getByUnicode(reactionObject.getEmoji().getName());
        if(emoji == null) {
            logger.debug("Unknown emoji " + reactionObject.getEmoji().getName() + ", skipping.");
            return;
        }

        ReactionImpl reaction = channel.addReaction(reactionObject, emoji);
        User user = client.getUser(reactionObject.getUserId());
        moduleManager.getMessageListeners().forEach(listener -> Wrapper2.wrap(listener::onReactionAdd, reaction, user));
    }

    public synchronized void onReactionRemove(ReactionUpdateResponse reactionObject) {
        final ChannelBase channel = (ChannelBase) client.getChannelById(reactionObject.getChannelId());
        if(channel == null) {
            // Not a tracked channel, message not tracked either
            return;
        }
        final Emoji emoji = Emoji.getByUnicode(reactionObject.getEmoji().getName());
        if(emoji == null) {
            logger.debug("Unknown emoji " + reactionObject.getEmoji().getName() + ", skipping.");
            return;
        }

        ReactionImpl reaction = channel.removeReaction(reactionObject, emoji);
        User user = client.getUser(reactionObject.getUserId());
        moduleManager.getMessageListeners().forEach(listener -> Wrapper2.wrap(listener::onReactionRemove, reaction, user));
    }
}
