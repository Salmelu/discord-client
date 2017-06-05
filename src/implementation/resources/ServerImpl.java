package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.RoleObject;
import cz.salmelu.discord.implementation.json.resources.ServerMemberObject;
import cz.salmelu.discord.implementation.json.resources.ServerObject;
import cz.salmelu.discord.implementation.json.response.ServerMemberUpdateResponse;
import cz.salmelu.discord.resources.*;

import java.util.*;

public class ServerImpl implements Server {

    private final String id;
    private final ServerObject originalObject;

    private final ClientImpl client;
    private final List<ServerChannel> channelList = new ArrayList<>();
    private final Map<String, ServerChannel> channelsByName = new HashMap<>();
    private final Map<String, ServerChannel> channelsById = new HashMap<>();
    private final List<Role> roleList = new ArrayList<>();
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();
    private final List<MemberImpl> memberList = new ArrayList<>();
    private final Map<String, MemberImpl> membersById = new HashMap<>();
    private final Map<String, MemberImpl> membersByNick = new HashMap<>();

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
            UserImpl user = client.getUser(member.getUser().getId());
            if(user == null) {
                user = new UserImpl(client, member.getUser());
                client.addUser(user);
            }
            final MemberImpl memberRef = new MemberImpl(client, this, user, member);
            memberList.add(memberRef);
            membersById.put(memberRef.getId(), memberRef);
            membersByNick.put(memberRef.getNickname() == null
                    ? memberRef.getUser().getName()
                    : memberRef.getNickname(),
                    memberRef);
            if(memberRef.getId().equals(client.getMyUser().getId())) {
                me = memberRef;
            }
        });

        Arrays.stream(serverObject.getChannels()).forEach((channel) -> {
            final ServerChannelImpl channelRef = new ServerChannelImpl(client, this, channel);
            channelList.add(channelRef);
            channelsByName.put(channelRef.getName(), channelRef);
            channelsById.put(channelRef.getId(), channelRef);
        });
        client.addChannels(channelList);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ServerImpl))return false;
        ServerImpl otherCast = (ServerImpl) other;
        return otherCast.getId().equals(getId());
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

    public boolean isDisabled() {
        return disabled;
    }

    public void addChannel(ServerChannel channel) {
        channelList.add(channel);
        channelsById.put(channel.getId(), channel);
        channelsByName.put(channel.getName(), channel);
        client.addChannel(channel);
    }

    public void removeChannel(ServerChannel channel) {
        channelList.remove(channel);
        channelsById.remove(channel.getId());
        channelsByName.remove(channel.getName());
        client.removeChannel(channel);
    }

    public void update(ServerObject serverObject) {
        originalObject.setName(serverObject.getName());
        originalObject.setIconHash(serverObject.getIconHash());
        originalObject.setOwnerId(serverObject.getOwnerId());
        originalObject.setRegion(serverObject.getRegion());
        originalObject.setAfkChannelId(serverObject.getAfkChannelId());
        originalObject.setAfkTimeout(serverObject.getAfkTimeout());
        originalObject.setEmbedEnabled(serverObject.isEmbedEnabled());
        originalObject.setEmbedChannelId(serverObject.getEmbedChannelId());
        originalObject.setVeriticationLevel(serverObject.getVeriticationLevel());
        originalObject.setDefaultMessageNotifications(serverObject.getDefaultMessageNotifications());
        originalObject.setRoles(serverObject.getRoles());
        originalObject.setEmojis(serverObject.getEmojis());
        originalObject.setFeatures(serverObject.getFeatures());
        originalObject.setMfaLevel(serverObject.getMfaLevel());

        updateRoles();
        channelList.forEach(channel -> ((ServerChannelImpl) channel).calculatePermissions());
    }

    private void updateRoles() {
        List<RoleImpl> newRoleList = new ArrayList<>();
        Arrays.stream(originalObject.getRoles()).forEach((role) -> {
            RoleImpl roleUpdate = (RoleImpl) rolesById.get(role.getId());
            if(roleUpdate != null) {
                roleUpdate.update(role);
                newRoleList.add(roleUpdate);
            }
            else {
                roleUpdate = new RoleImpl(this, role);
                newRoleList.add(roleUpdate);
            }
        });
        roleList.clear();
        rolesByName.clear();
        rolesById.clear();
        newRoleList.forEach(role -> {
            roleList.add(role);
            rolesById.put(role.getId(), role);
            rolesByName.put(role.getName(), role);
            if(role.getName().equals("@everyone")) everyoneRole = role;
        });
    }

    public Role addRole(RoleObject object) {
        final RoleImpl role = new RoleImpl(this, object);
        roleList.add(role);
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
        return role;
    }

    public void removeRole(String roleId) {
        final Role role = rolesById.remove(roleId);
        roleList.remove(role);
        rolesByName.remove(role.getName());
    }

    public void addMember(ServerMemberObject memberObject) {
        UserImpl user = client.getUser(memberObject.getUser().getId());
        if(user == null) {
            user = new UserImpl(client, memberObject.getUser());
            client.addUser(user);
        }
        final MemberImpl memberRef = new MemberImpl(client, this, user, memberObject);
        memberList.add(memberRef);
        membersById.put(memberRef.getId(), memberRef);
        membersByNick.put(memberRef.getNickname() == null
                        ? memberRef.getUser().getName()
                        : memberRef.getNickname(),
                memberRef);
        if(memberRef.getId().equals(client.getMyUser().getId())) {
            me = memberRef;
        }
    }

    public void removeMember(User userObject) {
        MemberImpl removed = membersById.remove(userObject.getId());
        memberList.remove(removed);
        membersByNick.remove(removed.getNickname());
    }

    public void updateMember(ServerMemberUpdateResponse memberObject) {
        MemberImpl member = membersById.get(memberObject.getUser().getId());
        member.setNickname(memberObject.getNick());
        member.setRoles(memberObject.getRoles());
        ((UserImpl) member.getUser()).update(memberObject.getUser());
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
    public List<ServerChannel> getChannels() {
        return Collections.unmodifiableList(channelList);
    }

    @Override
    public ServerChannel getChannelById(String id) {
        return channelsById.get(id);
    }

    @Override
    public ServerChannel getChannelByName(String name) {
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

    @Override
    public List<Member> getMembers() {
        return Collections.unmodifiableList(memberList);
    }

    @Override
    public Member getMemberById(String id) {
        return membersById.get(id);
    }

    @Override
    public Member getMemberByNickname(String nickname) {
        return membersByNick.get(nickname);
    }
}
