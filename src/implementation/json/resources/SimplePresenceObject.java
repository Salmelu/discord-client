package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.resources.PresenceStatus;

public class SimplePresenceObject implements MappedObject {
    UserObject user;
    GameObject game;
    PresenceStatus status;
}
