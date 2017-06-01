package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.implementation.json.resources.ServerObject;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Server;

import java.util.*;
import java.util.stream.Collectors;

public class ServerImpl implements Server {

    private final String id;
    private final ServerObject originalObject;

    private final ClientImpl client;
    private final List<Channel> channelList = new ArrayList<>();
    private final Map<String, Channel> channelsByName = new HashMap<>();
    private final Map<String, Channel> channelsById = new HashMap<>();
    private final Map<String, RoleImpl> roles = new HashMap<>();
    private final Map<String, MemberImpl> members = new HashMap<>();

    private RoleImpl everyoneRole;
    private MemberImpl me;

    public ServerImpl(ClientImpl client, ServerObject serverObject) {
        this.id = serverObject.getId();
        this.originalObject = serverObject;
        this.client = client;

        Arrays.stream(serverObject.getRoles()).forEach((role) -> {
            final RoleImpl roleRef = new RoleImpl(this, role);
            roles.put(roleRef.getId(), roleRef);
            if(roleRef.getName().equals("@everyone")) everyoneRole = roleRef;
        });

        Arrays.stream(serverObject.getMembers()).forEach((member) -> {
            UserImpl user = client.findUser(member.getUser().getId());
            if(user == null) {
                user = new UserImpl(client, member.getUser());
                client.addUser(user);
            }
            final MemberImpl memberRef = new MemberImpl(client, this, user, member);
            members.put(memberRef.getId(), memberRef);
            if(memberRef.getId().equals(client.getMyUser().getId())) {
                me = memberRef;
            }
        });

        Arrays.stream(serverObject.getChannels()).forEach((channel) -> {
            final ChannelImpl channelRef = new ChannelImpl(client, this, channel);
            channelList.add(channelRef);
            channelsByName.put(channelRef.getName(), channelRef);
            channelsById.put(channelRef.getId(), channelRef);
        });
        client.addChannels(channelList);
    }

    public MemberImpl getMe() {
        return me;
    }

    public RoleImpl getEveryoneRole() {
        return everyoneRole;
    }

    public RoleImpl getRole(String roleId) {
        return roles.get(roleId);
    }

    public long getPermissions() {
        // adds everyone permission + user role's permissions together
        return getMe().getRoles().stream().map(RoleImpl::getPermissions)
                .reduce(everyoneRole.getPermissions(), (p1, p2) -> p1 | p2);
    }

    public ClientImpl getClient() {
        return client;
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
    public List<Channel> getChannels() {
        return channelList;
    }

    @Override
    public Channel getChannelById(String id) {
        return channelsById.get(id);
    }

    @Override
    public Channel getChannelByName(String name) {
        return channelsByName.get(name);
    }
}
