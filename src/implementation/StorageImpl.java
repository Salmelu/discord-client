package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class StorageImpl implements Storage, Serializable {
    private transient Map<String, Object> storedObjects = new HashMap<>();
    private final String name;
    private Map<String, Object> tempObjects;

    public StorageImpl(String name) {
        this.name = name;
    }

    @Override
    public synchronized boolean hasValue(String name) {
        return storedObjects.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends Serializable> T getValue(String name) {
        return (T) storedObjects.get(name);
    }

    @Override
    public synchronized <T extends Serializable> void setValue(String name, T value) {
        storedObjects.put(name, value);
    }

    @Override
    public synchronized void removeValue(String name) {
        storedObjects.remove(name);
    }

    synchronized void save() {
        tempObjects = new HashMap<>(storedObjects);
    }

    synchronized void load() {
        storedObjects = tempObjects;
    }
}
