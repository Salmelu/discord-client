package cz.salmelu.discord.implementation;

import cz.salmelu.discord.SubscriptionManager;

/**
 * Responsible for spawning module-private subscription managers.
 */
class SubscriptionMaster {

    private final StorageManagerImpl storageManager;

    SubscriptionMaster(StorageManagerImpl manager) {
        this.storageManager = manager;
    }

    SubscriptionManager spawn(Class<?> owner) {
        final String name = owner.getName();
        return new SubscriptionManagerImpl(this, storageManager.getStorage(this, name));
    }
}
