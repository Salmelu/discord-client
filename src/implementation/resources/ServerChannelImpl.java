package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;

import java.util.*;
import java.util.stream.Collectors;

public class ServerChannelImpl extends ChannelBase implements ServerChannel {

    private final String id;
    private final ChannelObject originalObject;

    private final ServerImpl server;

    private Set<Permission> currentPermissions;

    public ServerChannelImpl(ClientImpl client, ServerImpl server, ChannelObject channelObject) {
        this.id = channelObject.getId();
        this.originalObject = channelObject;

        this.client = client;
        this.server = server;

        calculatePermissions();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ServerChannelImpl))return false;
        ServerChannelImpl otherCast = (ServerChannelImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public ServerChannel toServerChannel() {
        return this;
    }

    @Override
    public PrivateChannel toPrivateChannel() {
        return null;
    }

    // called when server becomes unavailable... prevents doing any actions with it
    public void resetPermissions() {
        currentPermissions = EnumSet.noneOf(Permission.class);
    }

    public void update(ChannelObject channelObject) {
        originalObject.setName(channelObject.getName());
        originalObject.setType(channelObject.getType());
        originalObject.setPosition(channelObject.getPosition());
        originalObject.setPermissionOverwrites(channelObject.getPermissionOverwrites());
        originalObject.setTopic(channelObject.getTopic());
        originalObject.setLastMessageId(channelObject.getLastMessageId());
        originalObject.setBitrate(channelObject.getBitrate());
        originalObject.setUserLimit(channelObject.getUserLimit());

        calculatePermissions();
    }

    /**
     * Precalculates current permissions for the channel given the discord rules.
     *
     * The rules are available at https://support.discordapp.com/hc/en-us/articles/206141927-How-is-the-permission-hierarchy-structured-
     *
     * Calculated permissions are saved in a local field to avoid recalculating everytime
     */
    public void calculatePermissions() {
        if(originalObject.getPermissionOverwrites() == null
                || originalObject.getPermissionOverwrites().length == 0) {
            currentPermissions = EnumSet.copyOf(server.getPermissions());
            return;
        }

        final long[] permissionValues = {0, 0, 0, 0, 0, 0};
        final RoleImpl everyoneRole = server.getEveryoneRole();
        final MemberImpl me = server.getMe();
        final List<String> roleIds = me.getRoles().stream().map(Role::getId).collect(Collectors.toList());

        Arrays.stream(originalObject.getPermissionOverwrites()).forEach(overwriteObject -> {
            if(overwriteObject.getType().equals(PermissionOverwriteType.ROLE)) {
                if(overwriteObject.getId().equals(everyoneRole.getId())) {
                    permissionValues[0] = overwriteObject.getDeny();
                    permissionValues[1] = overwriteObject.getAllow();
                }
                else if(roleIds.contains(overwriteObject.getId())) {
                    permissionValues[2] |= overwriteObject.getDeny();
                    permissionValues[3] |= overwriteObject.getAllow();
                }
            }
            else if(overwriteObject.getType().equals(PermissionOverwriteType.MEMBER)
                    && me.getId().equals(overwriteObject.getId())) {
                permissionValues[4] = overwriteObject.getDeny();
                permissionValues[5] = overwriteObject.getAllow();
            }
        });

        EnumSet<Permission> permissions = EnumSet.copyOf(server.getPermissions());
        permissions.removeAll(Permission.getPermissions(permissionValues[0]));
        permissions.addAll(Permission.getPermissions(permissionValues[1]));
        permissions.removeAll(Permission.getPermissions(permissionValues[2]));
        permissions.addAll(Permission.getPermissions(permissionValues[3]));
        permissions.removeAll(Permission.getPermissions(permissionValues[4]));
        permissions.addAll(Permission.getPermissions(permissionValues[5]));

        currentPermissions = Collections.unmodifiableSet(permissions);
    }

    public boolean checkPermission(Permission permission) {
        return currentPermissions.contains(permission);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return originalObject.getName();
    }

    @Override
    public String getTopic() {
        return originalObject.getTopic();
    }

    @Override
    public int getPosition() {
        return originalObject.getPosition();
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public void changeName(String newName) {
        editChannel(newName, getTopic(), getPosition());
    }

    @Override
    public void changeTopic(String newTopic) {
        editChannel(getName(), newTopic, getPosition());

    }

    @Override
    public void changePosition(int newPosition) {
        editChannel(getName(), getTopic(), newPosition);
    }

    @Override
    public void editChannel(String newName, String newTopic, int newPosition) {
        if(!checkPermission(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application doesn't have the permission to edit this channel.");
        }
        if(newName.length() < 2 || newName.length() > 100) {
            throw new IllegalArgumentException("The channel name must be between 2 and 100 characters long.");
        }
        if(newTopic.length() > 1024) {
            throw new IllegalArgumentException("Topic can't be longer than 1024 characters.");
        }
        originalObject.setName(newName);
        originalObject.setTopic(newTopic);
        originalObject.setPosition(newPosition);
        client.getRequester().patchRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).build(), originalObject.getModifyObject());
    }

    @Override
    public String getMention() {
        return "<#" + id + ">";
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean canSendMessage() {
        return checkPermission(Permission.SEND_MESSAGES);
    }

    @Override
    public void sendMessage(String text) {
        if(!canSendMessage()) {
            throw new PermissionDeniedException("This application doesn't have the permission to send messages to this channel.");
        }
        MessageObject messageObject = new MessageObject();
        messageObject.setContent(text);
        client.getRequester().postRequest(EndpointBuilder.create(Endpoint.CHANNEL).addElement(getId())
                .addElement("messages").build(), messageObject);
    }

    @Override
    public Message getMessage(String id) throws PermissionDeniedException {
        if(!checkPermission(Permission.READ_MESSAGE_HISTORY)) {
            throw new PermissionDeniedException("This application doesn't have the permission to read message history in this channel.");
        }
        return super.getMessage(id);
    }

    @Override
    public void deleteChannel() {
        if(!checkPermission(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application doesn't have the permission to delete this channel.");
        }
        super.deleteChannel();
    }
}
