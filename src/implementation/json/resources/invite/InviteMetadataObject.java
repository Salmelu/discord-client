package cz.salmelu.discord.implementation.json.resources.invite;

import cz.salmelu.discord.implementation.json.resources.UserObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InviteMetadataObject {
    UserObject inviter;
    int uses;
    int maxUses;
    int maxAge;
    boolean temporary;
    LocalDateTime created;
    boolean revoked;
}
