package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class StorageImpl implements Storage, Serializable {
    final Map<String, Object> storedObjects;
    private Lock lock;

    StorageImpl() {
        storedObjects = Collections.synchronizedMap(new HashMap<>());
    }

    StorageImpl(Map<String, Object> map) {
        storedObjects = map;
    }

    @Override
    public boolean hasValue(String name) {
        return storedObjects.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getValue(String name) {
        return (T) storedObjects.get(name);
    }

    @Override
    public <T extends Serializable> void setValue(String name, T value) {
        storedObjects.put(name, value);
    }

    @Override
    public void removeValue(String name) {
        storedObjects.remove(name);
    }

    @Override
    public synchronized void lock() {
        lock.lock();
    }

    @Override
    public synchronized void unlock() {
        lock.unlock();
    }
}
