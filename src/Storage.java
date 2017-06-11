package cz.salmelu.discord;

import java.io.Serializable;

public interface Storage {
    boolean hasValue(String name);
    <T extends Serializable> T getValue(String name);
    <T extends Serializable> void setValue(String name, T value);
    void removeValue(String name);
}
