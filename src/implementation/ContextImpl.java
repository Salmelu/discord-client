package cz.salmelu.discord.implementation;

import cz.salmelu.discord.*;

public class ContextImpl implements Context {
    private final StorageManagerImpl storageManager;
    private final NotifyManagerImpl notifyManager;
    private final SubscriptionMaster subscriptionMaster;
    private final Class<?> owner;

    public ContextImpl() {
        storageManager = new StorageManagerImpl();
        notifyManager = new NotifyManagerImpl();
        subscriptionMaster = new SubscriptionMaster(storageManager.getStorage(SubscriptionMaster.class));
        owner = null;
    }

    private ContextImpl(ContextImpl master, Class<?> owner) {
        storageManager = master.storageManager;
        notifyManager = master.notifyManager;
        subscriptionMaster = master.subscriptionMaster;
        this.owner = owner;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        notifyManager.setDispatcher(dispatcher);
    }

    public void startNotifyManager() {
        notifyManager.start();
    }

    public Context spawn(Class<?> owner) {
        return new ContextImpl(this, owner);
    }

    @Override
    public Storage getStorage() {
        if(owner == null) throw new java.lang.IllegalAccessError("Cannot invoke this method on master context.");
        return storageManager.getStorage(owner);
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
        if(owner == null) throw new java.lang.IllegalAccessError("Cannot invoke this method on master context.");
        return subscriptionMaster.spawn(owner);
    }
}
