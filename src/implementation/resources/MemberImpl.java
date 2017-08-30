package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.NameHelper;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemberImpl implements Member {

    private final ClientImpl client;
    private final ServerImpl server;
    private final UserImpl user;
    private final ServerMemberObject originalObject;

    private final List<Role> roles = new ArrayList<>();

    public MemberImpl(ClientImpl client, ServerImpl server, UserImpl user, ServerMemberObject object) {
        this.client = client;
        this.server = server;
        this.user = user;
        this.originalObject = object;

        Arrays.stream(originalObject.getRoles()).forEach(role -> roles.add(server.getRoleById(role)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof MemberImpl))return false;
        MemberImpl otherCast = (MemberImpl) other;
        return otherCast.getId().equals(getId())
                && otherCast.getServer().equals(getServer());
    }

    @Override
    public int hashCode() {
        return getId().hashCode() * 97 + getServer().getId().hashCode() * 119;
    }

    public void setNickname(String nickname) {
        this.originalObject.setNickname(nickname);
    }

    public void setRoles(String[] roleIds) {
        roles.clear();
        Arrays.stream(roleIds).forEach(role -> roles.add(server.getRoleById(role)));
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getNickname() {
        return originalObject.getNickname();
    }

    @Override
    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getMention() {
        return "<@!" + getId() + ">";
    }

    @Override
    public PrivateChannel createPrivateChannel() {
        return getUser().createPrivateChannel();
    }

    @Override
    public void addRole(Role role) throws PermissionDeniedException {
        if(roles.contains(role)) {
            return;
        }
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of this server.");
        }
        client.getRequester().putRequest(EndpointBuilder.create(Endpoint.SERVER)
                .addElement(server.getId()).addElement("members").addElement(getId())
                .addElement("roles").addElement(role.getId()).build());
        roles.add(role);
    }

    @Override
    public void removeRole(Role role) throws PermissionDeniedException {
        if(!roles.contains(role)) {
            return;
        }
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of this server.");
        }
        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.SERVER)
                .addElement(server.getId()).addElement("members").addElement(getId())
                .addElement("roles").addElement(role.getId()).build());
        roles.remove(role);
    }

    @Override
    public void setRoles(List<Role> roles) {
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of this server.");
        }
        JSONArray idArray = new JSONArray();
        for(Role role : roles) {
            if(!roles.contains(role)) continue;
            idArray.put(role.getId());
        }

        JSONObject request = new JSONObject();
        request.put("roles", idArray);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, request);
    }

    @Override
    public void mute(boolean mute) {
        if(!server.checkPermission(Permission.VOICE_MUTE)) {
            throw new PermissionDeniedException("This application cannot mute users of this server.");
        }
        JSONObject request = new JSONObject();
        request.put("mute", mute);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, request);
    }

    @Override
    public void deafen(boolean deaf) {
        if(!server.checkPermission(Permission.VOICE_DEAFEN)) {
            throw new PermissionDeniedException("This application cannot deafen users of this server.");
        }
        JSONObject request = new JSONObject();
        request.put("deaf", deaf);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, request);
    }

    @Override
    public void moveChannel(ServerChannel newChannel) {
        if(!newChannel.isVoice() || !newChannel.getServer().equals(getServer())) {
            throw new IllegalArgumentException("Invalid channel id given.");
        }
        if(!server.checkPermission(Permission.VOICE_MOVE)) {
            throw new PermissionDeniedException("This application cannot move voice channel users of this server.");
        }
        JSONObject request = new JSONObject();
        request.put("channel_id", newChannel.getId());
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, request);
    }

    @Override
    public void ban(int messageDays) {
        server.banMember(this, messageDays);
    }

    @Override
    public void kick() {
        server.kickMember(this);
    }

    @Override
    public void changeNickname(String nickname) {
        nickname = nickname.trim();
        if(!NameHelper.validateName(nickname)) {
            throw new IllegalArgumentException("Invalid nickname requested.");
        }
        if(!getServer().getPermissions().contains(Permission.MANAGE_NICKNAMES)) {
            throw new PermissionDeniedException("This application cannot change nicknames on this server.");
        }
        JSONObject request = new JSONObject();
        request.put("nick", nickname);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        client.getRequester().patchRequest(endpoint, request);
    }

}
