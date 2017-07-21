package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class IntegrationObject implements MappedObject {
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