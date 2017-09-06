package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StorageManagerImpl {

    private class NamePair {
        private Class<?> clazz;
        private String name;

        NamePair(Class<?> clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof NamePair))return false;
            NamePair otherCast = (NamePair) other;
            return otherCast.clazz.equals(clazz) && otherCast.name.equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + 71 * clazz.hashCode();
        }

        Class<?> getInnerClass() {
            return clazz;
        }

        String getInnerName() {
            return name;
        }
    }

    private final HashMap<NamePair, StorageImpl> storageMap = new HashMap<>();
    private static final String storagePath = "storage/";

    private final Thread savingThread;
    private static final int SLEEP_TIME = 5 * 60 * 1000;
    private boolean savingRunning;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    StorageManagerImpl() {
        savingThread = new Thread(() -> {
            while(savingRunning) {
                saveAll();
                try {
                    Thread.sleep(SLEEP_TIME);
                }
                catch (InterruptedException ignored) {

                }
            }
        });
        savingRunning = true;
        savingThread.start();
    }

    synchronized <T> Storage getStorage(T object, String name) {
        Class<?> clazz = object.getClass();
        return getStorageClassed(clazz, name);
    }

    synchronized Storage getStorageClassed(Class<?> clazz, String name) {
        NamePair pair = new NamePair(clazz, name);
        if(!storageMap.containsKey(pair)) {
            if(!load(pair)) {
                storageMap.put(pair, new StorageImpl());
            }
        }
        return storageMap.get(pair);
    }
    
    private void save(NamePair pair) {
        final StorageImpl storage = storageMap.get(pair);
        if(storage == null) return;

        final String filename = storagePath + pair.getInnerClass().getName() + "::" + pair.getInnerName();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename)))) {
            storage.lock();
            oos.writeObject(storage.storedObjects);
        }
        catch(IOException e) {
            logger.warn("Failed saving storage of class " + pair.getInnerClass().getName(), e);
        }
        finally {
            storage.unlock();
        }
    }

    synchronized void saveAll() {
        logger.info( "Saving all storages...");
        for (Map.Entry<NamePair, StorageImpl> classEntry : storageMap.entrySet()) {
            save(classEntry.getKey());
        }
        logger.info( "Done.");
    }

    @SuppressWarnings("unchecked")
    private boolean load(NamePair pair) {
        final String filename = storagePath + pair.getInnerClass().getName() + "::" + pair.getInnerName();
        if(!Files.exists(Paths.get(filename))) {
            logger.info("Storage file of " + pair.getInnerClass().getName() + " not found.");
            return false;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            final StorageImpl storage = new StorageImpl((Map<String, Object>) ois.readObject());
            storageMap.put(pair, storage);
            return true;
        }
        catch(IOException e) {
            logger.warn("Failed loading storage of class " + pair.getInnerClass().getName(), e);
        }
        catch (ClassNotFoundException e) {
            logger.error("Exception when loading a class.", e);
        }
        return false;
    }

    public void stop() {
        savingRunning = false;
        savingThread.interrupt();
        try {
            savingThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
