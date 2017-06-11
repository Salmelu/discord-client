package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

import java.util.List;

public interface Channel {
    String getId();
    boolean isPrivate();

    boolean canSendMessage();
    void sendMessage(String text) throws PermissionDeniedException;

    Message getMessage(String id) throws PermissionDeniedException;
    List<Message> getMessagesBefore(String messageId, int limit) throws PermissionDeniedException;
    List<Message> getMessagesAround(String messageId, int limit) throws PermissionDeniedException;
    List<Message> getMessagesAfter(String messageId, int limit) throws PermissionDeniedException;
    default List<Message> getMessagesBefore(Message message, int limit) throws PermissionDeniedException {
        return getMessagesBefore(message.getId(), limit);
    }
    default List<Message> getMessagesAround(Message message, int limit) throws PermissionDeniedException {
        return getMessagesAround(message.getId(), limit);
    }
    default List<Message> getMessagesAfter(Message message, int limit) throws PermissionDeniedException {
        return getMessagesAfter(message.getId(), limit);
    }
    void triggerTyping();

    ServerChannel toServerChannel();
    PrivateChannel toPrivateChannel();

}
