package cz.salmelu.discord.implementation;

import cz.salmelu.discord.PermissionGuard;
import cz.salmelu.discord.Storage;
import cz.salmelu.discord.resources.*;

import java.io.Serializable;
import java.util.*;

public class PermissionGuardImpl implements PermissionGuard {

    private static final String S_LEVEL = "level";
    private static final String S_PRIVATE = "private";
    private static final String S_ALLOW = "allow";
    private static final String S_ROLE = "roles";
    private static final String S_MEMBER = "members";
    private static final String S_CHANNEL = "channels";

    private final Storage storage;

    private int level;
    private boolean initialized = false;
    private boolean allowAll = true;
    private int privateMessages = 0;
    private Map<String, Integer> roleExceptions;
    private Map<String, Integer> memberExceptions;
    private Set<String> channelExceptions;

    PermissionGuardImpl(Storage storage) {
        this.storage = storage;
        try {
            storage.lock();
            if (storage.hasValue(S_LEVEL)) {
                level = storage.getValue(S_LEVEL);
                allowAll = storage.getValue(S_ALLOW);
                privateMessages = storage.getValue(S_PRIVATE);
                roleExceptions = storage.getValue(S_ROLE);
                memberExceptions = storage.getValue(S_MEMBER);
                channelExceptions = storage.getValue(S_CHANNEL);
                initialized = true;
            }
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void initialize(int level) {
        if(initialized) return;
        this.level = level;
        this.privateMessages = 0;
        this.roleExceptions = Collections.synchronizedMap(new HashMap<>());
        this.memberExceptions = Collections.synchronizedMap(new HashMap<>());
        this.channelExceptions = Collections.synchronizedSet(new HashSet<>());
        try {
            storage.lock();
            storage.setValue(S_LEVEL, level);
            storage.setValue(S_PRIVATE, privateMessages);
            storage.setValue(S_ALLOW, allowAll);
            storage.setValue(S_ROLE, (Serializable) roleExceptions);
            storage.setValue(S_MEMBER, (Serializable) memberExceptions);
            storage.setValue(S_CHANNEL, (Serializable) channelExceptions);
        }
        finally {
            storage.unlock();
        }
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void clearRules() {
        try {
            storage.lock();
            privateMessages = 0;
            roleExceptions.clear();
            memberExceptions.clear();
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void changeDefaultLevel(int level) {
        try {
            storage.lock();
            this.level = level;
            storage.setValue(S_LEVEL, level);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void addException(Role role, int level) {
        try {
            storage.lock();
            roleExceptions.put(role.getId(), level);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void addException(Member member, int level) {
        try {
            storage.lock();
            memberExceptions.put(member.getId(), level);
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void addExceptionRole(String id, int level) {
        try {
            storage.lock();
            roleExceptions.put(id, level);
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void addExceptionMember(String id, int level) {
        try {
            storage.lock();
            memberExceptions.put(id, level);
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void allowPrivateMessages(int level) {
        try {
            storage.lock();
            privateMessages = level;
            storage.setValue(S_PRIVATE, privateMessages);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void allowAllChannels(boolean allow) {
        try {
            storage.lock();
            allowAll = allow;
            storage.setValue(S_ALLOW, allowAll);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void removeException(Role role) {
        try {
            storage.lock();
            roleExceptions.remove(role.getId());
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void removeException(Member member) {
        try {
            storage.lock();
            memberExceptions.remove(member.getId());
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void removeExceptionRole(String id) {
        try {
            storage.lock();
            roleExceptions.remove(id);
        }
        finally {
            storage.unlock();
        }

    }

    @Override
    public void removeExceptionMember(String id) {
        try {
            storage.lock();
            memberExceptions.remove(id);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void addExcludedChannel(Channel channel) {
        try {
            storage.lock();
            channelExceptions.add(channel.getId());
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void addExcludedChannel(String id) {
        try {
            storage.lock();
            channelExceptions.add(id);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void removeExcludedChannel(Channel channel) {
        try {
            storage.lock();
            channelExceptions.remove(channel.getId());
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void removeExcludedChannel(String id) {
        try {
            storage.lock();
            channelExceptions.remove(id);
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public boolean isAllowed(Message message, int level) {
        return isAllowedChannel(message) && isAllowedMember(message, level);
    }

    @Override
    public boolean isAllowedChannel(Message message) {
        final Channel channel = message.getChannel();
        final boolean isChannelException = channelExceptions.contains(channel.getId());
        if(allowAll && isChannelException) {
            return false;
        }
        else if(!allowAll && !isChannelException) {
            return false;
        }
        // Channel is allowed, let's continue
        return true;
    }

    @Override
    public boolean isAllowedMember(Message message, int level) {
        final User author = message.getAuthor();
        final Channel channel = message.getChannel();

        // Check private channel levels first
        if(channel.isPrivate()) {
            return privateMessages >= level;
        }

        final Member member = channel.toServerChannel().getServer().getMember(author);

        // Does this member have any special exception set?
        Integer memberEx = memberExceptions.get(member.getId());
        if(memberEx != null) {
            return memberEx >= level;
        }

        // Find the role with highest permissions the user has
        OptionalInt roleEx = member.getRoles().stream()
                .filter(role -> roleExceptions.containsKey(role.getId()))
                .mapToInt(role -> roleExceptions.get(role.getId())).max();
        if(roleEx.isPresent()) {
            return roleEx.getAsInt() >= level;
        }

        // Nothing matched, check default permission
        return this.level >= level;
    }
}
