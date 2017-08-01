package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

import java.time.OffsetDateTime;

public class ServerObject implements MappedObject {
    private String id;
    private String name;
    private String iconHash;
    private String splashHash;
    private String ownerId;

    private String region;
    private String afkChannelId;
    private int afkTimeout;
    private boolean embedEnabled;
    private String embedChannelId;

    private int veriticationLevel;
    private int defaultMessageNotifications;

    private RoleObject[] roles;
    private ServerEmojiObject[] emojis;
    private String[] features;
    private int mfaLevel;

    private OffsetDateTime joinedAt;
    private boolean large;
    private boolean unavailable;
    private int memberCount;
    // VoiceState[] voiceStates;
    private ServerMemberObject[] members;
    private ChannelObject[] channels;
    private SimplePresenceObject[] presences;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconHash() {
        return iconHash;
    }

    public void setIconHash(String iconHash) {
        this.iconHash = iconHash;
    }

    public String getSplashHash() {
        return splashHash;
    }

    public void setSplashHash(String splashHash) {
        this.splashHash = splashHash;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAfkChannelId() {
        return afkChannelId;
    }

    public void setAfkChannelId(String afkChannelId) {
        this.afkChannelId = afkChannelId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    public void setAfkTimeout(int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    public boolean isEmbedEnabled() {
        return embedEnabled;
    }

    public void setEmbedEnabled(boolean embedEnabled) {
        this.embedEnabled = embedEnabled;
    }

    public String getEmbedChannelId() {
        return embedChannelId;
    }

    public void setEmbedChannelId(String embedChannelId) {
        this.embedChannelId = embedChannelId;
    }

    public int getVeriticationLevel() {
        return veriticationLevel;
    }

    public void setVeriticationLevel(int veriticationLevel) {
        this.veriticationLevel = veriticationLevel;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public void setDefaultMessageNotifications(int defaultMessageNotifications) {
        this.defaultMessageNotifications = defaultMessageNotifications;
    }

    public ServerEmojiObject[] getEmojis() {
        return emojis;
    }

    public void setEmojis(ServerEmojiObject[] emojis) {
        this.emojis = emojis;
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(String[] features) {
        this.features = features;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    public void setMfaLevel(int mfaLevel) {
        this.mfaLevel = mfaLevel;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        this.large = large;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public ServerMemberObject[] getMembers() {
        return members;
    }

    public void setMembers(ServerMemberObject[] members) {
        this.members = members;
    }

    public ChannelObject[] getChannels() {
        return channels;
    }

    public void setChannels(ChannelObject[] channels) {
        this.channels = channels;
    }

    public SimplePresenceObject[] getPresences() {
        return presences;
    }

    public void setPresences(SimplePresenceObject[] presences) {
        this.presences = presences;
    }

    public RoleObject[] getRoles() {
        return roles;
    }

    public void setRoles(RoleObject[] roles) {
        this.roles = roles;
    }
}