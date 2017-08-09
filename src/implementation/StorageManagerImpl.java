package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import cz.salmelu.discord.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StorageManagerImpl implements StorageManager {

    private final HashMap<Class<?>, StorageImpl> storageMap = new HashMap<>();
    private static final String storagePath = "storage/";

    private final Thread savingThread;
    private static final int SLEEP_TIME = 5 * 60 * 1000;
    private boolean savingRunning;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public StorageManagerImpl() {
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

    @Override
    public synchronized <T> Storage getStorage(T object) {
        Class<?> clazz = object.getClass();
        if(!storageMap.containsKey(clazz)) {
            if(!load(clazz)) {
                storageMap.put(clazz, new StorageImpl());
            }
        }
        return storageMap.get(clazz);
    }
    
    public synchronized void save(Class<?> clazz) {
        final StorageImpl storage = storageMap.get(clazz);
        if(storage == null) return;
        storage.save();

        final String filename = storagePath + clazz.getName();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename)))) {
            oos.writeObject(storage);
        }
        catch(IOException e) {
            logger.warn("Failed saving storage of class " + clazz.getName(), e);
        }
    }

    public synchronized void saveAll() {
        logger.info( "Saving all storages...");
        for (Map.Entry<Class<?>, StorageImpl> classEntry : storageMap.entrySet()) {
            save(classEntry.getKey());
        }
        logger.info( "Done.");
    }

    @SuppressWarnings("unchecked")
    private boolean load(Class<?> clazz) {
        final String filename = storagePath + clazz.getName();
        if(!Files.exists(Paths.get(filename))) {
            logger.info("Storage file of " + clazz.getName() + " not found.");
            return false;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            final StorageImpl storage = (StorageImpl) ois.readObject();
            storageMap.put(clazz, storage);
            storage.load();
            return true;
        }
        catch(IOException e) {
            logger.warn("Failed loading storage of class " + clazz.getName(), e);
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
