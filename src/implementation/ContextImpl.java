package cz.salmelu.discord.implementation;

import cz.salmelu.discord.*;

public class ContextImpl implements Context {
    private final StorageManagerImpl storageManager;
    private final NotifyManagerImpl notifyManager;
    private final SubscriptionMaster subscriptionMaster;

    private final Class<?> owner;
    private PermissionGuard permissionGuard = null;
    private SubscriptionManager subscriptionManager = null;

    public ContextImpl() {
        storageManager = new StorageManagerImpl();
        notifyManager = new NotifyManagerImpl();
        subscriptionMaster = new SubscriptionMaster(storageManager);
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
    public Storage getStorage(String name) {
        if(owner == null) throw new java.lang.IllegalAccessError("Cannot invoke this method on master context.");
        return storageManager.getStorageClassed(owner, name);
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
        if(subscriptionManager == null) {
            subscriptionManager = subscriptionMaster.spawn(owner);
        }
        return subscriptionManager;
    }

    @Override
    public PermissionGuard getPermissionGuard() {
        if(owner == null) throw new java.lang.IllegalAccessError("Cannot invoke this method on master context.");
        if(permissionGuard == null) {
            final Storage storage = storageManager.getStorage(PermissionGuard.class, owner.getName());
            permissionGuard = new PermissionGuardImpl(storage);
        }
        return permissionGuard;
    }
}
