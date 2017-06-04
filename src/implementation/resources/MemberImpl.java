package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.resources.Member;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.User;

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
    public String getMention() {
        return "<@!" + getId() + ">";
    }

}
