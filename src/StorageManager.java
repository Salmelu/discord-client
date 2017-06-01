package cz.salmelu.discord;

public interface StorageManager {
    <T> Storage getStorage(T object, String name);
}
