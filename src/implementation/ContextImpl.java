package cz.salmelu.discord.implementation;

import cz.salmelu.discord.*;

/**
 * Implementation of the context.
 */
public class ContextImpl implements Context {
    private final StorageManagerImpl storageManager;
    private final NotifyManagerImpl notifyManager;
    private final SubscriptionMaster subscriptionMaster;
    private ModuleManager moduleManager;

    private final Class<?> owner;
    private PermissionGuard permissionGuard = null;
    private SubscriptionManager subscriptionManager = null;

    ContextImpl(String storagePath) {
        storageManager = new StorageManagerImpl(storagePath);
        notifyManager = new NotifyManagerImpl();
        subscriptionMaster = new SubscriptionMaster(storageManager);
        owner = null;
    }

    /**
     * A child instance for a specific module.
     * @param master parent instance
     * @param owner owning module
     */
    private ContextImpl(ContextImpl master, Class<?> owner) {
        storageManager = master.storageManager;
        notifyManager = master.notifyManager;
        subscriptionMaster = master.subscriptionMaster;
        moduleManager = master.moduleManager;
        this.owner = owner;
    }

    void setDispatcher(Dispatcher dispatcher) {
        notifyManager.setDispatcher(dispatcher);
    }

    void setModuleManager(ModuleManager manager) {
        this.moduleManager = manager;
    }

    void startNotifyManager() {
        notifyManager.start();
    }

    /**
     * Creates a new context instance for a specific module.
     * This is used to create personal storage.
     * @param owner owning module
     * @return a child instance
     */
    Context spawn(Class<?> owner) {
        return new ContextImpl(this, owner);
    }

    @Override
    public Storage getStorage(String name) {
        if(owner == null) throw new java.lang.IllegalAccessError("Cannot invoke this method on master context.");
        return storageManager.getStorageClassed(owner, name);
    }

    StorageManagerImpl getStorageManagerImpl() {
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

    @Override
    public <T> T getModuleInstance(Class<T> moduleClass) {
        return moduleManager.getModule(moduleClass);
    }
}
