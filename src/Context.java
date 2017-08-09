package cz.salmelu.discord;

/**
 * FIXME: REWRITE
 * <p>Current application context. Holds references to instantiated managers.</p>
 */
public interface Context {
    /**
     * Gets application's storage manager.
     * @return an instance of {@link StorageManager}
     */
    Storage getStorage();

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
