package cz.salmelu.discord.implementation.json.resources.invite;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.ChannelType;

public class InviteChannelObject implements MappedObject {
    String id;
    String name;
    ChannelType type;
}
