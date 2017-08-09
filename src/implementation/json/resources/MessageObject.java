package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;
import cz.salmelu.discord.implementation.json.resources.embed.EmbedObject;

import java.time.OffsetDateTime;

public class MessageObject implements MappedObject {
    private String id;
    private String channelId;
    private Integer type;
    private UserObject author;
    private String content;
    private OffsetDateTime timestamp;
    private OffsetDateTime editedTimestamp;
    private Boolean tts;
    private Boolean mentionEveryone;
    private UserObject[] mentions;
    private String[] mentionRoles;
    private AttachmentObject[] attachments;
    private EmbedObject[] embeds;
    private ReactionObject[] reactions;
    private String nonce;
    private Boolean pinned;
    private String webhookId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public UserObject getAuthor() {
        return author;
    }

    public void setAuthor(UserObject author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public OffsetDateTime getEditedTimestamp() {
        return editedTimestamp;
    }

    public void setEditedTimestamp(OffsetDateTime editedTimestamp) {
        this.editedTimestamp = editedTimestamp;
    }

    public Boolean isTts() {
        return tts;
    }

    public void setTts(Boolean tts) {
        this.tts = tts;
    }

    public Boolean isMentionEveryone() {
        return mentionEveryone;
    }

    public void setMentionEveryone(Boolean mentionEveryone) {
        this.mentionEveryone = mentionEveryone;
    }

    public UserObject[] getMentions() {
        return mentions;
    }

    public void setMentions(UserObject[] mentions) {
        this.mentions = mentions;
    }

    public String[] getMentionRoles() {
        return mentionRoles;
    }

    public void setMentionRoles(String[] mentionRoles) {
        this.mentionRoles = mentionRoles;
    }

    public AttachmentObject[] getAttachments() {
        return attachments;
    }

    public void setAttachments(AttachmentObject[] attachments) {
        this.attachments = attachments;
    }

    public EmbedObject[] getEmbeds() {
        return embeds;
    }

    public void setEmbeds(EmbedObject[] embeds) {
        this.embeds = embeds;
    }

    public ReactionObject[] getReactions() {
        return reactions;
    }

    public void setReactions(ReactionObject[] reactions) {
        this.reactions = reactions;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Boolean isPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
