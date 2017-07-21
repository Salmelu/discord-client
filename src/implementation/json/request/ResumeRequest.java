package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class ResumeRequest implements MappedObject {
    private String token;
    private String sessionId;
    private int seq;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
