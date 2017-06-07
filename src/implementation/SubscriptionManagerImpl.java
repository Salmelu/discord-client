package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import cz.salmelu.discord.SubscriptionManager;
import cz.salmelu.discord.resources.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionManagerImpl implements SubscriptionManager {

    private HashMap<String, ArrayList<String>> registeredChannels;
    private HashMap<String, HashMap<String, ArrayList<String>>> subscribers;

    private final Storage storage;
    private static final String CHANNEL_STORAGE = "channels";
    private static final String SUBS_STORAGE = "subscribers";

    public SubscriptionManagerImpl(Storage storage) {
        this.storage = storage;

        registeredChannels = storage.getValue(CHANNEL_STORAGE);
        if(registeredChannels == null) {
            registeredChannels = new HashMap<>();
            storage.setValue(CHANNEL_STORAGE, registeredChannels);
        }

        subscribers = storage.getValue(SUBS_STORAGE);
        if(subscribers == null) {
            subscribers = new HashMap<>();
            storage.setValue(SUBS_STORAGE, subscribers);
        }
    }

    @Override
    public <T> void registerChannel(T object, Channel channel) {
        if(channel.isPrivate()) {
            throw new RuntimeException("Cannot subscribe a private channel.");
        }
        ArrayList<String> channels = registeredChannels.computeIfAbsent(object.getClass().getName(), k -> new ArrayList<>());
        channels.add(channel.getId());
        subscribers.computeIfAbsent(object.getClass().getName(), k -> new HashMap<>())
                .computeIfAbsent(channel.getId(), k -> new ArrayList<>());
        storage.setValue(CHANNEL_STORAGE, channels);
        storage.setValue(SUBS_STORAGE, channels);
    }

    @Override
    public <T> void unregisterChannel(T object, Channel channel) {
        ArrayList<String> channels = registeredChannels.get(object.getClass().getName());
        if(channels != null) {
            channels.remove(channel.getId());
            subscribers.get(object.getClass().getName()).remove(channel.getId());
            storage.setValue(CHANNEL_STORAGE, channels);
            storage.setValue(SUBS_STORAGE, channels);
        }
    }

    @Override
    public <T> void addSubscriber(T object, Message message, String errorMessage) {
        if(!registeredChannels.containsKey(object.getClass().getName())) {
            message.reply("This function doesn't offer any subscriptions.");
            return;
        }
        final List<String> channels = registeredChannels.get(object.getClass().getName());
        if(!channels.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final ArrayList<String> subs = subscribers.get(object.getClass().getName())
                .computeIfAbsent(message.getChannel().getId(), k -> new ArrayList<>());
        if(subs.contains(message.getAuthor().getId())) {
            message.reply("You are already subscribed.");
        }
        else {
            subs.add(message.getAuthor().getId());
            storage.setValue(SUBS_STORAGE, subscribers);
            message.reply("You have been successfully subscribed.");
        }
    }

    @Override
    public <T> void removeSubscriber(T object, Message message, String errorMessage) {
        if(!registeredChannels.containsKey(object.getClass().getName())) {
            message.reply("This function doesn't offer any subscriptions.");
            return;
        }
        final List<String> channels = registeredChannels.get(object.getClass().getName());
        if(!channels.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final ArrayList<String> subs = subscribers.get(object.getClass().getName())
                .get(message.getChannel().getId());
        if(subs == null || !subs.contains(message.getAuthor().getId())) {
            message.reply("You are already unsubscribed.");
        }
        else {
            subs.remove(message.getAuthor().getId());
            storage.setValue(SUBS_STORAGE, subscribers);
            message.reply("You have been successfully unsubscribed.");
        }
    }

    @Override
    public <T> String getSubscribers(T object, Channel channel) throws RuntimeException {
        if(!registeredChannels.containsKey(object.getClass().getName())) {
            throw new RuntimeException("Cannot request subscription of non-subscribed class");
        }
        final ArrayList<String> subs = subscribers.get(object.getClass().getName()).get(channel.getId());

        final Server server = channel.toServerChannel().getServer();
        purgeSubscribers(server, subs);

        return subs.stream()
                .map(id -> server.getMemberById(id).getMention())
                .collect(Collectors.joining(" "));
    }

    private void purgeSubscribers(Server server, ArrayList<String> subs) {
        final ArrayList<String> toRemove = new ArrayList<>();
        subs.forEach(id -> {
            if(server.getMemberById(id) == null) {
                toRemove.add(id);
            }
        });
        if(toRemove.size() > 0) {
            toRemove.forEach(subs::remove);
            storage.setValue(SUBS_STORAGE, subscribers);
        }
    }
}
