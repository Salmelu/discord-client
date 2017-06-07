package cz.salmelu.discord;

public interface Context {
    StorageManager getStorageManager();
    NotifyManager getNotifyManager();
    SubscriptionManager getSubscriptionManager();
}
