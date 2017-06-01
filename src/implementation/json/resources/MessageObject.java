package cz.salmelu.discord.implementation.json.resources;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.resources.embed.EmbedObject;

import java.time.LocalDateTime;

public class MessageObject extends JSONMappedObject {
    private String id;
    private String channelId;
    private UserObject author;
    private String content;
    private LocalDateTime timestamp;
    private LocalDateTime editedTimestamp;
    private boolean tts;
    private boolean atEveryone;
    private UserObject[] mentions;
    private String[] mentionedRoles;
    private AttachmentObject[] attachments;
    private EmbedObject[] embeds;
    private ReactionObject[] reactions;
    private String nonce;
    private boolean pinned;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getEditedTimestamp() {
        return editedTimestamp;
    }

    public void setEditedTimestamp(LocalDateTime editedTimestamp) {
        this.editedTimestamp = editedTimestamp;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public boolean isAtEveryone() {
        return atEveryone;
    }

    public void setAtEveryone(boolean atEveryone) {
        this.atEveryone = atEveryone;
    }

    public UserObject[] getMentions() {
        return mentions;
    }

    public void setMentions(UserObject[] mentions) {
        this.mentions = mentions;
    }

    public String[] getMentionedRoles() {
        return mentionedRoles;
    }

    public void setMentionedRoles(String[] mentionedRoles) {
        this.mentionedRoles = mentionedRoles;
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

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }
}
