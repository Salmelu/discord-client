package cz.salmelu.discord.implementation.json.resources;

public class IntegrationObject {
    String id;
    String name;
    String type;
    boolean enabled;
    boolean syncing;
    String roleId;
    int expireBehavior;
    int expireGraceperiod;
    UserObject user;
    IntegrationAccountObject account;
    long lastSynced;
}