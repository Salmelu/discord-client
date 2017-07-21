package cz.salmelu.discord.implementation.json.resources.embed;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class EmbedImageObject implements MappedObject {
    String url;
    String proxyUrl;
    int width;
    int height;
}