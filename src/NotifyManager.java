package cz.salmelu.discord;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface NotifyManager {
    NotificationHandle addNotification(Object o, Callback callback, OffsetDateTime when);
    NotificationHandle addNotification(Object o, Callback callback, LocalDateTime when);
    NotificationHandle addNotification(Object o, Callback callback, long when);

    void removeNotification(NotificationHandle handle);

    interface Callback {
        void call(Object object);
    }

    interface NotificationHandle {

    }
}
