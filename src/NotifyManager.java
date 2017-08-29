package cz.salmelu.discord;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * <p>A manager to provide an easy interface for sending delayed or periodic messages.</p>
 *
 * <p>Every notification can have a custom object attached
 * which can be used to pass any information required.</p>
 */
public interface NotifyManager {
    /**
     * <p>Adds a new notification.</p>
     * @param o attached object if needed, can be null
     * @param callback a callback called when the notification fires
     * @param when the datetime when the notification should be fired
     * @return a handle to the notification, required to cancel it
     */
    NotificationHandle addNotification(Object o, Callback callback, OffsetDateTime when);

    /**
     * <p>Adds a new notification.</p>
     * @param o attached object if needed, can be null
     * @param callback a callback called when the notification fires
     * @param when the datetime when the notification should be fired
     * @return a handle to the notification, required to cancel it
     */
    NotificationHandle addNotification(Object o, Callback callback, LocalDateTime when);

    /**
     * <p>Adds a new notification.</p>
     * @param o attached object if needed, can be null
     * @param callback a callback called when the notification fires
     * @param when unix timestamp denoting when the notification should be fired
     * @return a handle to the notification, required to cancel it
     */
    NotificationHandle addNotification(Object o, Callback callback, long when);

    /**
     * <p>Removes a notification represented by the passed handle.</p>
     * @param handle handle of removed notification
     */
    void removeNotification(NotificationHandle handle);

    /**
     * An interface for callback called by notifications.
     */
    interface Callback {
        void call(Object object);
    }

    /**
     * A handle used to store reference to scheduled notifications.
     */
    interface NotificationHandle {

    }
}
