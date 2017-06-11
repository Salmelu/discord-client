package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import cz.salmelu.discord.SubscriptionManager;
import cz.salmelu.discord.resources.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionManagerImpl implements SubscriptionManager {

    private final Storage channelStorage;
    private final Storage subsStorage;

    public SubscriptionManagerImpl(Storage channelStorage, Storage subsStorage) {
        this.channelStorage = channelStorage;
        this.subsStorage = subsStorage;
    }

    @Override
    public <T> void registerChannel(T object, Channel channel) {
        if(channel.isPrivate()) {
            throw new RuntimeException("Cannot subscribe a private channel.");
        }

        final String className = object.getClass().getName();

        ArrayList<String> channels = channelStorage.getValue(className);
        if(channels == null) channels = new ArrayList<>();
        if(channels.contains(channel.getId())) return;

        channels.add(channel.getId());
        channelStorage.setValue(object.getClass().getName(), channels);

        if(!subsStorage.hasValue(className)) {
            final HashMap<String, ArrayList<String>> map = new HashMap<>();
            map.put(channel.getId(), new ArrayList<>());
            subsStorage.setValue(className, map);
        }
        else {
            final HashMap<String, ArrayList<String>> map = subsStorage.getValue(className);
            map.put(channel.getId(), new ArrayList<>());
        }
    }

    @Override
    public <T> void unregisterChannel(T object, Channel channel) {
        ArrayList<String> channels = channelStorage.getValue(object.getClass().getName());
        if(channels != null) {
            channels.remove(channel.getId());
            final HashMap<String, ArrayList<String>> map = subsStorage.getValue(object.getClass().getName());
            map.remove(channel.getId());
            channelStorage.setValue(object.getClass().getName(), channels);
        }
    }

    @Override
    public <T> void addSubscriber(T object, Message message, String errorMessage) {
        if(!channelStorage.hasValue(object.getClass().getName())) {
            message.reply("This function doesn't offer any subscriptions.");
            return;
        }
        final List<String> channels = channelStorage.getValue(object.getClass().getName());
        if(!channels.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final HashMap<String, ArrayList<String>> map = subsStorage.getValue(object.getClass().getName());
        final ArrayList<String> subs = map.get(message.getChannel().getId());
        if(subs.contains(message.getAuthor().getId())) {
            message.reply("You are already subscribed.");
        }
        else {
            subs.add(message.getAuthor().getId());
            message.reply("You have been successfully subscribed.");
        }
    }

    @Override
    public <T> void removeSubscriber(T object, Message message, String errorMessage) {
        if(!channelStorage.hasValue(object.getClass().getName())) {
            message.reply("This function doesn't offer any subscriptions.");
            return;
        }
        final List<String> channels = channelStorage.getValue(object.getClass().getName());
        if(!channels.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final HashMap<String, ArrayList<String>> map = subsStorage.getValue(object.getClass().getName());
        final ArrayList<String> subs = map.get(message.getChannel().getId());
        if(subs == null || !subs.contains(message.getAuthor().getId())) {
            message.reply("You are already unsubscribed.");
        }
        else {
            subs.remove(message.getAuthor().getId());
            message.reply("You have been successfully unsubscribed.");
        }
    }

    @Override
    public <T> String getSubscribers(T object, Channel channel) throws RuntimeException {
        if(!channelStorage.hasValue(object.getClass().getName())) {
            throw new RuntimeException("Cannot request subscription of non-subscribed class");
        }
        final HashMap<String, ArrayList<String>> map = subsStorage.getValue(object.getClass().getName());
        final ArrayList<String> subs = map.get(channel.getId());

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
        }
    }
}
