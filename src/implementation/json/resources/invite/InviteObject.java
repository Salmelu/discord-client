package cz.salmelu.discord.implementation.json.resources.invite;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.ServerObject;

public class InviteObject implements MappedObject {
    String code;
    ServerObject server;
    ChannelObject channel;
}
