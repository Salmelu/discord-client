package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class ServerEmojiObject implements MappedObject {
    String id;
    String name;
    RoleObject[] roles;
    boolean requireColons;
    boolean managed;
}
