package cz.salmelu.discord.implementation.json.resources.embed;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

import java.time.OffsetDateTime;

public class EmbedObject implements MappedObject {
    String title;
    String type;
    String description;
    String url;
    OffsetDateTime timestamp;
    int color;
    EmbedFooterObject footer;
    EmbedImageObject image;
    EmbedThumbnailObject thumbnail;
    EmbedVideoObject video;
    EmbedProviderObject provider;
    EmbedAuthorObject author;
    EmbedFieldObject[] fields;


}