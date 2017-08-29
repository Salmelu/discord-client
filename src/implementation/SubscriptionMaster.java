package cz.salmelu.discord.implementation;

import cz.salmelu.discord.SubscriptionManager;

public class SubscriptionMaster {

    private final StorageManagerImpl storageManager;

    public SubscriptionMaster(StorageManagerImpl manager) {
        this.storageManager = manager;
    }

    public SubscriptionManager spawn(Class<?> owner) {
        final String name = owner.getName();
        return new SubscriptionManagerImpl(this, storageManager.getStorage(this, name));
    }
}
