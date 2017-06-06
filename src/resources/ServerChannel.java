package cz.salmelu.discord.resources;

import cz.salmelu.discord.PermissionDeniedException;

public interface ServerChannel extends Channel {
    String getName();
    String getTopic();
    int getPosition();
    Server getServer();

    void changeName(String newName) throws IllegalArgumentException, PermissionDeniedException;;
    void changeTopic(String newTopic) throws IllegalArgumentException, PermissionDeniedException;;
    void changePosition(int newPosition) throws IllegalArgumentException, PermissionDeniedException;;
    void editChannel(String newName, String newTopic, int newPosition) throws IllegalArgumentException, PermissionDeniedException;

    String getMention();
}
