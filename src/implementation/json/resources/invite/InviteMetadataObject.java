package cz.salmelu.discord.implementation.json.resources.invite;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;

import java.time.OffsetDateTime;

public class InviteMetadataObject implements MappedObject {
    UserObject inviter;
    int uses;
    int maxUses;
    int maxAge;
    boolean temporary;
    OffsetDateTime created;
    boolean revoked;
}
