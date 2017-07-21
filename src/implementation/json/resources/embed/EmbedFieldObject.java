package cz.salmelu.discord.implementation.json.resources.embed;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class EmbedFieldObject implements MappedObject {
    String name;
    String value;
    boolean inline;
}
