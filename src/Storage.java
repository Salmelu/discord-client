package cz.salmelu.discord;

import java.io.Serializable;

/**
 * <p>Represents a persistent storage of data.</p>
 *
 * <p>Each storage is a persistent, automatically saved, HashMap with key value pairs.</p>
 */
public interface Storage {
    /**
     * Checks if the storage has a value saved associated with given name.
     * @param name checked identifier
     * @return true if this storage has a value saved under the name
     */
    boolean hasValue(String name);

    /**
     * Gets value of the storage with given name.
     * @param name requested identifier
     * @return the saved value or null
     */
    <T extends Serializable> T getValue(String name);

    /**
     * Sets and stores a value with specified name.
     * @param name identifier for saved value
     * @param value saved value
     */
    <T extends Serializable> void setValue(String name, T value);

    /**
     * Removes a value from the storage with given name.
     * @param name identifier for the value
     */
    void removeValue(String name);
}
