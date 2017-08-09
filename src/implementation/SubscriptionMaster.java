package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import cz.salmelu.discord.SubscriptionManager;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Message;
import cz.salmelu.discord.resources.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionMaster {

    private final Storage storage;

    private static final String CHANNEL_STORAGE = "channel_storage";
    private static final String SUBS_STORAGE = "subs_storage";
    private final HashMap<String, ArrayList<String>> channelStorage;
    private final HashMap<String, HashMap<String, ArrayList<String>>> subsStorage;

    public SubscriptionMaster(Storage storage) {
        this.storage = storage;
        if(storage.hasValue(CHANNEL_STORAGE)) {
            this.channelStorage = storage.getValue(CHANNEL_STORAGE);
            this.subsStorage = storage.getValue(SUBS_STORAGE);
        }
        else {
            this.channelStorage = new HashMap<>();
            this.subsStorage = new HashMap<>();
            storage.setValue(CHANNEL_STORAGE, channelStorage);
            storage.setValue(SUBS_STORAGE, subsStorage);
        }
    }

    public SubscriptionManager spawn(Class<?> owner) {
        final String name = owner.getName();
        if(!channelStorage.containsKey(owner.getName())) {
            channelStorage.put(name, new ArrayList<>());
        }
        if(!subsStorage.containsKey(owner.getName())) {
            subsStorage.put(name, new HashMap<>());
        }
        saveStorages();
        return new SubscriptionManagerImpl(this, channelStorage.get(name), subsStorage.get(name));
    }

    void saveStorages() {
        storage.setValue(CHANNEL_STORAGE, channelStorage);
        storage.setValue(SUBS_STORAGE, subsStorage);
    }
}
