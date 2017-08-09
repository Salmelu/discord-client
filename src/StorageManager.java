package cz.salmelu.discord;

/**
 * A manager to provide an easy interface for storing values.
 */
public interface StorageManager {
    /**
     * <p>Returns a storage for a specified module.</p>
     * @param object a module requesting the storage
     * @return reference to the storage
     */
    <T> Storage getStorage(T object);
}
