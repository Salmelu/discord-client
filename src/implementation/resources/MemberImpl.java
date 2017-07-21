package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;

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
    public void addRole(Role role) throws PermissionDeniedException {
        if(roles.contains(role)) {
            return;
        }
        if(!server.checkPermission(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles of that server.");
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
            throw new PermissionDeniedException("This application cannot manage roles of that server.");
        }
        client.getRequester().deleteRequest(EndpointBuilder.create(Endpoint.SERVER)
                .addElement(server.getId()).addElement("members").addElement(getId())
                .addElement("roles").addElement(role.getId()).build());
        roles.remove(role);
    }

}
