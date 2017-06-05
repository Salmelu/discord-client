package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.PermissionHelper;
import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.net.Endpoint;
import cz.salmelu.discord.resources.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerChannelImpl implements ServerChannel {

    private final String id;
    private final ChannelObject originalObject;

    private final ServerImpl server;
    private final ClientImpl client;

    private long currentPermissions;

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

    // called when server becomes unavailable... prevents doing any actions with it
    public void resetPermissions() {
        currentPermissions = 0;
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
        if(originalObject.getPermissionOverwrites().length == 0) {
            currentPermissions = server.getPermissions();
            return;
        }

        final long[] permissions = {server.getPermissions(), 0, 0, 0, 0, 0, 0};
        final RoleImpl everyoneRole = server.getEveryoneRole();
        final MemberImpl me = server.getMe();
        final List<String> roleIds = me.getRoles().stream().map(Role::getId).collect(Collectors.toList());

        Arrays.stream(originalObject.getPermissionOverwrites()).forEach(overwriteObject -> {
            if(overwriteObject.getType().equals(PermissionOverwriteType.ROLE)) {
                if(overwriteObject.getId().equals(everyoneRole.getId())) {
                    permissions[1] = overwriteObject.getDeny();
                    permissions[2] = overwriteObject.getAllow();
                }
                else if(roleIds.contains(overwriteObject.getId())) {
                    permissions[3] |= overwriteObject.getDeny();
                    permissions[4] |= overwriteObject.getAllow();
                }
            }
            else if(overwriteObject.getType().equals(PermissionOverwriteType.MEMBER)
                    && me.getId().equals(overwriteObject.getId())) {
                permissions[5] = overwriteObject.getDeny();
                permissions[6] = overwriteObject.getAllow();
            }
        });

        permissions[0] &= ~permissions[1];
        permissions[0] |= permissions[2];
        permissions[0] &= ~permissions[3];
        permissions[0] |= permissions[4];
        permissions[0] &= ~permissions[5];
        permissions[0] |= permissions[6];

        currentPermissions = permissions[0];
    }

    private boolean checkPermission(Predicate<Long> permissionChecker) {
        return permissionChecker.test(currentPermissions);
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
    public Server getServer() {
        return server;
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
        return checkPermission(PermissionHelper::canSendMessages);
    }

    @Override
    public void sendMessage(String text) {
        if(!canSendMessage()) {
            throw new PermissionDeniedException("This application doesn't have the permission to send messages to this channel.");
        }
        MessageObject messageObject = new MessageObject();
        messageObject.setContent(text);
        client.getRequester().postRequest(Endpoint.CHANNEL + "/" + id + "/messages", messageObject);
    }

    @Override
    public ServerChannel toServerChannel() {
        return this;
    }

    @Override
    public PrivateChannel toPrivateChannel() {
        return null;
    }
}