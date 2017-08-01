package cz.salmelu.discord;

/**
 * A manager to provide an easy interface for storing values.
 */
public interface StorageManager {
    /**
     * <p>Returns a storage with a specified name.</p>
     *
     * <p>If a module needs to save more different types of data, a module can request various storages.</p>
     * @param object a module requesting the storage
     * @param name storage name, used for identification
     * @return reference to the storage
     */
    <T> Storage getStorage(T object, String name);
}
