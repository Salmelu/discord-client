package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class AttachmentObject implements MappedObject {
    String id;
    String filename;
    int size;
    String url;
    String proxyUrl;
    int width;
    int height;
}
