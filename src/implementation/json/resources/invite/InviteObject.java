package cz.salmelu.discord.implementation.json.resources.invite;

import cz.salmelu.discord.implementation.json.resources.ChannelObject;
import cz.salmelu.discord.implementation.json.resources.ServerObject;

public class InviteObject {
    String code;
    ServerObject server;
    ChannelObject channel;
}
