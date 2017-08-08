package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.resources.PermissionOverwriteObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
        final RoleImpl everyoneRole = (RoleImpl) server.getEveryoneRole();
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
    public boolean isVoice() {
        return originalObject.getType() == ChannelType.SERVER_VOICE;
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
        editChannelCommon(newName, null, -1, -1, -1);
    }

    @Override
    public void changeTopic(String newTopic) {
        if(originalObject.getType() != ChannelType.SERVER_TEXT) {
            throw new PermissionDeniedException("You can only set topic in text channels.");
        }
        editChannelCommon(null, newTopic, -1, -1, -1);
    }

    @Override
    public void changePosition(int newPosition) {
        editChannelCommon(null, null, newPosition, -1, -1);
    }

    @Override
    public void changeBitrate(int newBitRate) throws IllegalArgumentException, PermissionDeniedException {
        if(originalObject.getType() != ChannelType.SERVER_VOICE) {
            throw new PermissionDeniedException("You can only set bitrate in voice channels.");
        }
        editChannelCommon(null, null, -1, newBitRate, -1);
    }

    @Override
    public void changeUserLimit(int newUserLimit) throws IllegalArgumentException, PermissionDeniedException {
        if(originalObject.getType() != ChannelType.SERVER_VOICE) {
            throw new PermissionDeniedException("You can only set user limit in voice channels.");
        }
        editChannelCommon(null, null, -1, -1, newUserLimit);
    }

    @Override
    public void editTextChannel(String newName, String newTopic, int newPosition) {
        if(originalObject.getType() != ChannelType.SERVER_TEXT) {
            throw new PermissionDeniedException("This channel is not a text channel.");
        }
        editChannelCommon(newName, newTopic, newPosition, -1, -1);
    }

    @Override
    public void editVoiceChannel(String newName, int newPosition, int newBitrate, int newUserLimit) {
        if(originalObject.getType() != ChannelType.SERVER_VOICE) {
            throw new PermissionDeniedException("This channel is not a voice channel.");
        }
        editChannelCommon(newName, null, newPosition, newBitrate, newUserLimit);
    }

    private void editChannelCommon(String name, String topic, int position, int bitrate, int userLimit) {
        if(!checkPermission(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application doesn't have the permission to edit this channel.");
        }
        if(name != null && (name.length() < 2 || name.length() > 100)) {
            throw new IllegalArgumentException("The channel name must be between 2 and 100 characters long.");
        }
        if(topic != null && (topic.length() > 1024)) {
            throw new IllegalArgumentException("Topic can't be longer than 1024 characters.");
        }
        if(position < -1) {
            throw new IllegalArgumentException("Position must be a positive number.");
        }
        if(bitrate != -1 && (bitrate < 8000 || bitrate > 96000)) {
            throw new IllegalArgumentException("Channel bitrate must be between 8000 and 96000.");
        }
        if(userLimit < -1 || userLimit > 99) {
            throw new IllegalArgumentException("User limit must be between 0 and 99.");
        }
        if(name != null) originalObject.setName(name);
        if(topic != null) originalObject.setTopic(topic);
        if(position != -1) originalObject.setPosition(position);
        if(bitrate != -1) originalObject.setBitrate(bitrate);
        if(userLimit != -1) originalObject.setUserLimit(userLimit);
        client.getRequester().patchRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).build(), originalObject.getModifyObject());
    }

    @Override
    public void updatePermissionOverwrites(PermissionOverwrite old, PermissionOverwrite replaced) {
        if(!old.getType().equals(replaced.getType())) {
            throw new IllegalArgumentException("Cannot replace overwrites of different types.");
        }
        if(!checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application doesn't have the permission to change this channel's permissions.");
        }

        PermissionOverwriteObject poo = new PermissionOverwriteObject();
        poo.setType(replaced.getType());
        poo.setAllow(Permission.convertToValue(replaced.getAllow()));
        poo.setDeny(Permission.convertToValue(replaced.getDeny()));

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("permissions").addElement(old.getId()).build();
        client.getRequester().putRequest(endpoint, client.getSerializer().serialize(poo));
    }

    @Override
    public void deletePermissionOverwrites(PermissionOverwrite overwrites) {
        if(!checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application doesn't have the permission to change this channel's permissions.");
        }

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("permissions").addElement(overwrites.getId()).build();
        client.getRequester().deleteRequest(endpoint);
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
    public void bulkDeleteMessages(List<Message> messages) {
        bulkDeleteMessagesByIds(messages.stream().map(Message::getId).collect(Collectors.toList()));
    }

    @Override
    public void bulkDeleteMessagesByIds(List<String> messageIds) {
        if(!checkPermission(Permission.MANAGE_MESSAGES)) {
            throw new PermissionDeniedException("This application doesn't have the permission to delete messages in this channel.");
        }
        final JSONArray jsonArray = new JSONArray();
        messageIds.forEach(jsonArray::put);
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("messages", jsonArray);

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(getId()).addElement("messages").addElement("bulk-delete").build();
        client.getRequester().postRequest(endpoint, jsonObject);
    }

    @Override
    public void pinMessage(Message message) {
        if(!isPrivate()) {
            if(!((ServerChannelImpl) toServerChannel()).checkPermission(Permission.MANAGE_MESSAGES)) {
                throw new PermissionDeniedException("This application doesn't have the permission manage messages in this channel.");
            }
        }
        super.pinMessage(message);
    }

    @Override
    public void unpinMessage(Message message) {
        if(!isPrivate()) {
            if(!((ServerChannelImpl) toServerChannel()).checkPermission(Permission.MANAGE_MESSAGES)) {
                throw new PermissionDeniedException("This application doesn't have the permission manage messages in this channel.");
            }
        }
        super.unpinMessage(message);
    }

    @Override
    public void deleteChannel() {
        if(!checkPermission(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application doesn't have the permission to delete this channel.");
        }
        super.deleteChannel();
    }
}
