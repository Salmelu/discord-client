package cz.salmelu.discord.implementation;

import cz.salmelu.discord.NotifyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotifyManagerImpl implements NotifyManager {

    private static int handleUID = 1;

    private class Notification {
        long timestamp;
        Callback callback;
        Object object;
        NotificationHandle handle;
    }

    private class NotificationHandleImpl implements NotificationHandle {
        long uid;

        NotificationHandleImpl(long uid) {
            this.uid = uid;
        }

        @Override
        public int hashCode() {
            return (int) this.uid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (!(obj instanceof NotificationHandleImpl)) return false;
            NotificationHandleImpl objc = (NotificationHandleImpl) obj;
            return objc.uid == this.uid;
        }
    }

    private volatile boolean running = false;

    private Thread notifierThread;
    private final Condition queueCondition;
    private final Lock queueLock;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker marker = MarkerFactory.getMarker("NotifyManager");

    private final PriorityQueue<Notification> notificationQueue =
            new PriorityQueue<>((n1, n2) -> {
                if(n1.timestamp < n2.timestamp) return -1;
                else if(n1.timestamp > n2.timestamp) return 1;
                else return 0;
            });
    private final Map<NotificationHandle, Notification> notificationMap = new HashMap<>();
    private Dispatcher dispatcher;

    public NotifyManagerImpl() {
        queueLock = new ReentrantLock();
        queueCondition = queueLock.newCondition();
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start() {
        running = true;
        notifierThread = new Thread(this::run);
        notifierThread.start();
    }

    public void stop() {
        running = false;
        if(notifierThread != null) {
            queueCondition.signalAll();
            try {
                notifierThread.join();
            }
            catch(InterruptedException e) {
                logger.warn(marker, "Interrupted notifier joining.");
            }
        }
    }

    @Override
    public NotificationHandle addNotification(Object o, Callback callback, OffsetDateTime when) {
        return addNotification(o, callback, when.toEpochSecond() * 1000);
    }

    @Override
    public NotificationHandle addNotification(Object o, Callback callback, LocalDateTime when) {
        return addNotification(o, callback, when.atOffset(when.atZone(ZoneId.of("Europe/Prague")).getOffset()));
    }

    @Override
    public NotificationHandle addNotification(Object o, Callback callback, long when) {
        queueLock.lock();
        try {
            final Notification notification = new Notification();
            notification.callback = callback;
            notification.timestamp = when;
            notification.object = o;
            notification.handle = new NotificationHandleImpl(++handleUID);
            notificationQueue.add(notification);
            notificationMap.put(notification.handle, notification);
            queueCondition.signal();
            return notification.handle;
        }
        finally {
            queueLock.unlock();
        }
    }

    @Override
    public void removeNotification(NotificationHandle handle) {
        queueLock.lock();
        try {
            final Notification notification = notificationMap.get(handle);
            if(notification != null) {
                notificationQueue.remove(notification);
            }
        }
        finally {
            queueLock.unlock();
        }
    }

    private void run() {
        int sleepTime;
        while(running) {
            final long currentTime = System.currentTimeMillis();
            final List<Notification> processNotification = new ArrayList<>();

            // Take everything needed from queue
            queueLock.lock();
            Notification next = notificationQueue.peek();
            while (next != null && next.timestamp <= currentTime) {
                processNotification.add(next);
                notificationQueue.poll();
                notificationMap.remove(next.handle);
                next = notificationQueue.peek();
            }
            sleepTime = 60 * 60 * 1000; // 1 hour
            if(next != null && next.timestamp - currentTime < 60 * 60 * 1000) {
                sleepTime = (int) (next.timestamp - currentTime);
            }
            queueLock.unlock();

            // Process notifications
            processNotification.forEach(notification ->
                    dispatcher.fireNotification(notification.callback, notification.object));

            // Schedule next
            queueLock.lock();
            try {
                queueCondition.await(sleepTime, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                logger.debug(marker, "Interrupted notifier waiting.");
            }
            finally {
                queueLock.unlock();
            }
        }
    }
}
