package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.NotifyManager;
import cz.salmelu.discord.StorageManager;
import cz.salmelu.discord.SubscriptionManager;

public class ContextImpl implements Context {
    private final StorageManagerImpl storageManager;
    private final NotifyManagerImpl notifyManager;
    private final SubscriptionManager subscriptionManager;

    public ContextImpl() {
        storageManager = new StorageManagerImpl();
        notifyManager = new NotifyManagerImpl();
        subscriptionManager = new SubscriptionManagerImpl(
                storageManager.getStorage(this, "subscription_manager_channels"),
                storageManager.getStorage(this, "subscription_manager_subs"));
    }

    public void setDispatcher(Dispatcher dispatcher) {
        notifyManager.setDispatcher(dispatcher);
    }

    public void startNotifyManager() {
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

    @Override
    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}
