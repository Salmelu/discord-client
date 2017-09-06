package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.*;
import cz.salmelu.discord.implementation.json.resources.*;
import cz.salmelu.discord.implementation.json.response.ServerMemberUpdateResponse;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Future;

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
    private Set<Permission> permissions = null;
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

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public MemberImpl getMe() {
        return me;
    }

    @Override
    public Set<Permission> getPermissions() {
        if(disabled) return EnumSet.noneOf(Permission.class);
        if(permissions != null) return permissions;
        // adds everyone permission + user role's permissions together
        EnumSet<Permission> set = EnumSet.copyOf(everyoneRole.getPermissions());
        for(Role role : getMe().getRoles()) {
            set.addAll(role.getPermissions());
        }
        permissions = Collections.unmodifiableSet(set);
        return permissions;
    }

    public boolean checkPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public void disable() {
        disabled = true;
    }

    @Override
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
        permissions = null;
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

    public Role removeRole(String roleId) {
        final Role role = rolesById.remove(roleId);
        roleList.remove(role);
        rolesByName.remove(role.getName());
        return role;
    }

    public Member addMember(ServerMemberObject memberObject) {
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
        return memberRef;
    }

    public void removeMember(User userObject) {
        MemberImpl removed = membersById.remove(userObject.getId());
        memberList.remove(removed);
        membersByNick.remove(removed.getNickname());
    }

    public Member updateMember(ServerMemberUpdateResponse memberObject) {
        MemberImpl member = membersById.get(memberObject.getUser().getId());

        final String oldNick = member.getNickname();
        if(oldNick != null) {
            membersByNick.remove(oldNick);
        }
        else {
            membersByNick.remove(member.getUser().getName());
        }

        final String newNick = memberObject.getNick();
        if(newNick != null) {
            membersByNick.put(newNick, member);
        }
        else {
            membersByNick.put(member.getUser().getName(), member);
        }

        member.setNickname(memberObject.getNick());

        member.setRoles(memberObject.getRoles());
        ((UserImpl) member.getUser()).update(memberObject.getUser());
        if(memberObject.getUser().getId().equals(me.getUser().getId())) {
            permissions = null;
            channelList.forEach(channel -> ((ServerChannelImpl) channel).calculatePermissions());
        }
        return member;
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
    public Future<RequestResponse> leave() {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.USER).addElement("@me")
                                      .addElement("guilds").addElement(getId()).build();
        return getClient().getRequester().deleteRequestAsync(endpoint);
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
    public Role getEveryoneRole() {
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
    public void loadAllMembers() {
        client.getSocket().requestOfflineMembers(getId(), "", 0);
    }

    @Override
    public List<Member> getMembers() {
        return Collections.unmodifiableList(memberList);
    }

    @Override
    public Member getMemberById(String id) {
        Member member = membersById.get(id);
        if(member == null) {
            // Let's try to get it remotely
            try {
                final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                        .addElement(getId())
                        .addElement("members")
                        .addElement(id)
                        .build();
                final JSONObject jsonObject = getClient().getRequester().getRequestAsObject(endpoint);
                final ServerMemberObject memberObject = client.getSerializer()
                        .deserialize(jsonObject, ServerMemberObject.class);
                return addMember(memberObject);
            }
            catch(DiscordRequestException e) {
                // No such user was found
                return null;
            }
        }
        return member;
    }

    @Override
    public Member getMember(User user) {
        return getMemberById(user.getId());
    }

    @Override
    public Member getMemberByNickname(String nickname) {
        return membersByNick.get(nickname);
    }

    @Override
    public Future<RequestResponse> kickMember(Member member) {
        if(!getPermissions().contains(Permission.KICK_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot kick members on this server.");
        }
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("members").addElement(member.getId()).build();
        return client.getRequester().deleteRequestAsync(endpoint);
    }

    @Override
    public List<User> getBannedUsers() {
        if(!getPermissions().contains(Permission.BAN_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot access banlist of this server.");
        }
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("bans").build();
        JSONArray array = client.getRequester().getRequestAsArray(endpoint);
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            UserObject userObject = client.getSerializer().deserialize(jsonObject, UserObject.class);
            UserImpl user = client.getUser(userObject.getId());
            if(user == null) {
                user = new UserImpl(client, userObject);
                client.addUser(user);
            }
            userList.add(user);
        }
        return userList;
    }

    @Override
    public Future<RequestResponse> banMember(Member member, int messageDays) {
        return banUser(member.getUser(), messageDays);
    }

    @Override
    public Future<RequestResponse> banUser(User user, int messageDays) {
        if(messageDays < 0 || messageDays > 7) {
            throw new IllegalArgumentException("Message days must be a value between 0 and 7.");
        }
        if(!getPermissions().contains(Permission.BAN_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot ban members on this server.");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("delete-message-days", messageDays);

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("bans").addElement(user.getId()).build();
        return client.getRequester().putRequestAsync(endpoint, jsonObject);
    }

    @Override
    public Future<RequestResponse> unbanUser(User user) {
        if(!getPermissions().contains(Permission.BAN_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot unban members on this server.");
        }
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("bans").addElement(user.getId()).build();
        return client.getRequester().deleteRequestAsync(endpoint);
    }

    @Override
    public Future<RequestResponse> changeMyNickname(String nickname) {
        nickname = nickname.trim();
        if(!NameHelper.validateName(nickname)) {
            throw new IllegalArgumentException("Invalid nickname requested.");
        }
        if(!getPermissions().contains(Permission.CHANGE_NICKNAME)) {
            throw new PermissionDeniedException("This application cannot change nicknames on this server.");
        }
        JSONObject request = new JSONObject();
        request.put("nick", nickname);
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER).addElement(getId())
                .addElement("members").addElement("@me").addElement("nick").build();
        return client.getRequester().patchRequestAsync(endpoint, request);
    }

    @Override
    public Future<RequestResponse> createTextChannel(String name, List<PermissionOverwrite> overwrites) {
        if(!getPermissions().contains(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application cannot create channels on this server.");
        }
        final JSONObject object = new JSONObject();
        object.put("name", Channel.ChannelType.SERVER_TEXT);
        object.put("type", "text");
        return createChannelCommon(object, overwrites);
    }

    @Override
    public Future<RequestResponse> createVoiceChannel(String name, int bitrate, int userLimit,
                                                      List<PermissionOverwrite> overwrites) {
        if(!getPermissions().contains(Permission.MANAGE_CHANNELS)) {
            throw new PermissionDeniedException("This application cannot create channels on this server.");
        }
        final JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("type", Channel.ChannelType.SERVER_VOICE);
        object.put("bitrate", bitrate);
        object.put("user_limit", userLimit);
        return createChannelCommon(object, overwrites);
    }

    private Future<RequestResponse> createChannelCommon(JSONObject object, List<PermissionOverwrite> overwrites) {
        final JSONArray overwritesArray = new JSONArray();
        overwrites.forEach(overwrite -> {
            PermissionOverwriteObject poo = new PermissionOverwriteObject();
            poo.setId(overwrite.getId());
            poo.setType(overwrite.getType());
            poo.setAllow(Permission.convertToValue(overwrite.getAllow()));
            poo.setDeny(Permission.convertToValue(overwrite.getDeny()));
            overwritesArray.put(poo);
        });

        object.put("permission_overwrites", overwritesArray);
        return client.getRequester().postRequestAsync(
                EndpointBuilder.create(Endpoint.SERVER).addElement(getId()).addElement("channels").build());
    }

    @Override
    public Future<RequestResponse> deleteChannel(ServerChannel channel) {
        if(!channelList.contains(channel)) {
            throw new IllegalArgumentException("Given channel doesn't belong this server.");
        }
        return channel.deleteChannel();
    }

    @Override
    public Future<RequestResponse> createRole(String name, List<Permission> permissions, int color,
                                              boolean separate, boolean mentionable) {
        if(!getPermissions().contains(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot create roles on this server.");
        }
        if(name == null) {
            throw new IllegalArgumentException("Role name must be a valid string.");
        }

        long permissionBits = 0;
        if(permissions != null) {
            for(Permission p : permissions) {
                permissionBits |= p.getValue();
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("permissions", permissionBits);
        jsonObject.put("color", color);
        jsonObject.put("hoist", separate);
        jsonObject.put("mentionable", mentionable);

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("roles").build();
        return client.getRequester().postRequestAsync(endpoint, jsonObject);
    }

    @Override
    public Future<RequestResponse> updateRole(Role role, String name, List<Permission> permissions, int color,
                                              boolean separate, boolean mentionable) {
        if(!getPermissions().contains(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot manage roles on this server.");
        }
        if(!rolesById.containsKey(role.getId())) {
            throw new IllegalArgumentException("The updated role is not from this server.");
        }
        if(name == null) {
            throw new IllegalArgumentException("Role name must be a valid string.");
        }

        long permissionBits = 0;
        if(permissions != null) {
            for(Permission p : permissions) {
                permissionBits |= p.getValue();
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("permissions", permissionBits);
        jsonObject.put("color", color);
        jsonObject.put("hoist", separate);
        jsonObject.put("mentionable", mentionable);

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("roles").build();
        return client.getRequester().patchRequestAsync(endpoint, jsonObject);
    }

    @Override
    public Future<RequestResponse> deleteRole(Role role) {
        if(!getPermissions().contains(Permission.MANAGE_ROLES)) {
            throw new PermissionDeniedException("This application cannot delete roles on this server.");
        }
        if(!rolesById.containsKey(role.getId())) {
            throw new IllegalArgumentException("The deleted role is not from this server.");
        }

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("roles").addElement(role.getId()).build();
        return client.getRequester().deleteRequestAsync(endpoint);
    }

    @Override
    public int getPruneMembersCount(int days) {
        if(days <= 0) {
            throw new IllegalArgumentException("The number of days must be a positive number.");
        }
        if(!getPermissions().contains(Permission.KICK_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot kick members on this server.");
        }

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("prune").addParam("days", String.valueOf(days)).build();
        JSONObject result = client.getRequester().getRequestAsObject(endpoint);
        return result.getInt("pruned");
    }

    @Override
    public int pruneMembers(int days) {
        if(days <= 0) {
            throw new IllegalArgumentException("The number of days must be a positive number.");
        }
        if(!getPermissions().contains(Permission.KICK_MEMBERS)) {
            throw new PermissionDeniedException("This application cannot kick members on this server.");
        }

        final Endpoint endpoint = EndpointBuilder.create(Endpoint.SERVER)
                .addElement(getId()).addElement("prune").addParam("days", String.valueOf(days)).build();
        JSONObject result = client.getRequester().postRequestAsObject(endpoint);
        return result.getInt("pruned");
    }
}
