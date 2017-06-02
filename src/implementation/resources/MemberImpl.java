package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.resources.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemberImpl {

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

        Arrays.stream(originalObject.getRoles()).forEach(role -> roles.add(server.getRoleById(role.getId())));
    }

    public String getId() {
        return user.getId();
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public String getMention() {
        return "<@!" + getId() + ">";
    }

}
