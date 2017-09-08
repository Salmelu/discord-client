package cz.salmelu.discord;

/**
 * <p>Current application context. Provides instances of various managers.</p>
 */
public interface Context {
    /**
     * <p>Gets module's personal storage identified by a name.</p>
     * <p>Every module can have multiple storages with different names.</p>.
     * @return an instance of {@link Storage}
     */
    Storage getStorage(String name);

    /**
     * Gets application's notify manager.
     * @return an instance of {@link NotifyManager}
     */
    NotifyManager getNotifyManager();

    /**
     * Gets module's subscription manager.
     * @return an instance of {@link SubscriptionManager}
     */
    SubscriptionManager getSubscriptionManager();

    /**
     * Gets module's permission guard.
     * @return an instance of {@link PermissionGuard}
     */
    PermissionGuard getPermissionGuard();

    /**
     * <p>Gets an instance of running module.</p>
     * <p>Use this to communicate with another module.</p>
     * <p>Note that this can return null if the module has not been loaded yet.</p>
     * @param moduleClass class of desired module
     * @return instance of running module or null, if the instance
     * is not running, or not yet initialized
     */
    <T> T getModuleInstance(Class<T> moduleClass);
}
