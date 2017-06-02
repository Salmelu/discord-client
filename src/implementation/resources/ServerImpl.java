package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.ServerObject;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Role;
import cz.salmelu.discord.resources.Server;

import java.util.*;

public class ServerImpl implements Server {

    private final String id;
    private final ServerObject originalObject;

    private final ClientImpl client;
    private final List<Channel> channelList = new ArrayList<>();
    private final Map<String, Channel> channelsByName = new HashMap<>();
    private final Map<String, Channel> channelsById = new HashMap<>();
    private final List<Role> roleList = new ArrayList<>();
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();
    private final Map<String, MemberImpl> members = new HashMap<>();

    private boolean disabled = false;
    private RoleImpl everyoneRole;
    private MemberImpl me;

    public ServerImpl(ClientImpl client, ServerObject serverObject) {
        this.id = serverObject.getId();
        this.originalObject = serverObject;
        this.client = client;

        Arrays.stream(serverObject.getRoles()).forEach((role) -> {
            final RoleImpl roleRef = new RoleImpl(this, role);
            roleList.add(roleRef);
            rolesById.put(roleRef.getId(), roleRef);
            rolesByName.put(roleRef.getName(), roleRef);
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

    public long getPermissions() {
        if(disabled) return 0;
        // adds everyone permission + user role's permissions together
        return getMe().getRoles().stream().map(role -> ((RoleImpl) (role)).getPermissions())
                .reduce(everyoneRole.getPermissions(), (p1, p2) -> p1 | p2);
    }

    public void disable() {
        disabled = true;
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
        return Collections.unmodifiableList(channelList);
    }

    @Override
    public Channel getChannelById(String id) {
        return channelsById.get(id);
    }

    @Override
    public Channel getChannelByName(String name) {
        return channelsByName.get(name);
    }

    @Override
    public RoleImpl getEveryoneRole() {
        return everyoneRole;
    }

    @Override
    public List<Role> getRoles() {
        return Collections.unmodifiableList(roleList);
    }

    @Override
    public Role getRoleById(String id) {
        return rolesById.get(id);
    }

    @Override
    public Role getRoleByName(String name) {
        return rolesByName.get(name);
    }
}
