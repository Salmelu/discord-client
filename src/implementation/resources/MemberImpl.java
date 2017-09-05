package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.*;
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
import java.util.concurrent.Future;

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
    public Future<RequestResponse> addRole(Role role, AsyncCallback callback) throws PermissionDeniedException {
        if(roles.contains(role)) {
            return null;
        }
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of this server.");
        }

        final AsyncCallback wrapped = new AsyncCallback() {
            @Override
            public void completed(RequestResponse response) {
                roles.add(role);
                callback.completed(response);
            }

            @Override
            public void failed(DiscordRequestException e) {
                callback.failed(e);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        };

        return client.getRequester().putRequestAsync(EndpointBuilder.create(Endpoint.SERVER)
                .addElement(server.getId()).addElement("members").addElement(getId())
                .addElement("roles").addElement(role.getId()).build(), wrapped);
    }

    @Override
    public Future<RequestResponse> removeRole(Role role, AsyncCallback callback) throws PermissionDeniedException {
        if(!roles.contains(role)) {
            return null;
        }
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of this server.");
        }

        final AsyncCallback wrapped = new AsyncCallback() {
            @Override
            public void completed(RequestResponse response) {
                roles.remove(role);
                callback.completed(response);
            }

            @Override
            public void failed(DiscordRequestException e) {
                callback.failed(e);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        };

        return client.getRequester().deleteRequestAsync(EndpointBuilder.create(Endpoint.SERVER)
                .addElement(server.getId()).addElement("members").addElement(getId())
                .addElement("roles").addElement(role.getId()).build(), wrapped);
    }

    @Override
    public Future<RequestResponse> setRoles(List<Role> roles, AsyncCallback callback) {
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
        return client.getRequester().patchRequestAsync(endpoint, request, callback);
    }

    @Override
    public Future<RequestResponse> mute(boolean mute, AsyncCallback callback) {
        if(!server.checkPermission(Permission.VOICE_MUTE)) {
            throw new PermissionDeniedException("This application cannot mute users of this server.");
        }
        JSONObject request = new JSONObject();
        request.put("mute", mute);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        return client.getRequester().patchRequestAsync(endpoint, request, callback);
    }

    @Override
    public Future<RequestResponse> deafen(boolean deaf, AsyncCallback callback) {
        if(!server.checkPermission(Permission.VOICE_DEAFEN)) {
            throw new PermissionDeniedException("This application cannot deafen users of this server.");
        }
        JSONObject request = new JSONObject();
        request.put("deaf", deaf);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getServer().getId())
                .addElement("members").addElement(getId()).build();
        return client.getRequester().patchRequestAsync(endpoint, request, callback);
    }

    @Override
    public Future<RequestResponse> moveChannel(ServerChannel newChannel, AsyncCallback callback) {
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
        return client.getRequester().patchRequestAsync(endpoint, request, callback);
    }

    @Override
    public Future<RequestResponse> ban(int messageDays, AsyncCallback callback) {
        return server.banMember(this, messageDays, callback);
    }

    @Override
    public Future<RequestResponse> kick(AsyncCallback callback) {
        return server.kickMember(this, callback);
    }

    @Override
    public Future<RequestResponse> changeNickname(String nickname, AsyncCallback callback) {
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
        return client.getRequester().patchRequestAsync(endpoint, request, callback);
    }

}
