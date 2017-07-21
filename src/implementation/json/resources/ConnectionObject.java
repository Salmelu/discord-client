package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class ConnectionObject implements MappedObject {
    String id;
    String name;
    String type;
    boolean revoked;
    IntegrationObject[] integrations;
}
