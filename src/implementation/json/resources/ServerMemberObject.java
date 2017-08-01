package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

import java.time.OffsetDateTime;

public class ServerMemberObject implements MappedObject {
    private UserObject user;
    private String nickname;
    private String[] roles;
    private OffsetDateTime joinedAt;
    private boolean deaf;
    private boolean muted;

    public UserObject getUser() {
        return user;
    }

    public void setUser(UserObject user) {
        this.user = user;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isDeaf() {
        return deaf;
    }

    public void setDeaf(boolean deaf) {
        this.deaf = deaf;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}