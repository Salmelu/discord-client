package cz.salmelu.discord;

/**
 * <p>Current application context. Holds references to instantiated managers.</p>
 */
public interface Context {
    /**
     * Gets application's storage manager.
     * @return an instance of {@link StorageManager}
     */
    StorageManager getStorageManager();

    /**
     * Gets application's notify manager.
     * @return an instance of {@link NotifyManager}
     */
    NotifyManager getNotifyManager();

    /**
     * Gets application's subscription manager.
     * @return an instance of {@link SubscriptionManager}
     */
    SubscriptionManager getSubscriptionManager();
}
