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
}
