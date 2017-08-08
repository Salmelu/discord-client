package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

import java.util.List;

public interface ServerChannel extends Channel {
    String getName();
    String getTopic();
    boolean isVoice();
    int getPosition();
    Server getServer();

    void changeName(String newName) throws IllegalArgumentException, PermissionDeniedException;
    void changeTopic(String newTopic) throws IllegalArgumentException, PermissionDeniedException;
    void changePosition(int newPosition) throws IllegalArgumentException, PermissionDeniedException;
    void changeBitrate(int newBitRate) throws IllegalArgumentException, PermissionDeniedException;
    void changeUserLimit(int newUserLimit) throws IllegalArgumentException, PermissionDeniedException;
    void editTextChannel(String newName, String newTopic, int newPosition);
    void editVoiceChannel(String newName, int newPosition, int newBitrate, int newUserLimit);

    void updatePermissionOverwrites(PermissionOverwrite old, PermissionOverwrite replaced);

    void deletePermissionOverwrites(PermissionOverwrite overwrites);

    String getMention();

    void bulkDeleteMessages(List<Message> messages);

    void bulkDeleteMessagesByIds(List<String> messageIds);
}
