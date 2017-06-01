package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.NotifyManager;
import cz.salmelu.discord.StorageManager;

public class ContextImpl implements Context {
    private final StorageManagerImpl storageManager;
    private final NotifyManagerImpl notifyManager;

    public ContextImpl() {
        storageManager = new StorageManagerImpl();
        notifyManager = new NotifyManagerImpl();
        notifyManager.start();
    }

    @Override
    public StorageManager getStorageManager() {
        return storageManager;
    }

    public StorageManagerImpl getStorageManagerImpl() {
        return storageManager;
    }

    @Override
    public NotifyManager getNotifyManager() {
        return notifyManager;
    }
}
