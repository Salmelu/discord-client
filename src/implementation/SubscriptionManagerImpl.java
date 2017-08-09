package cz.salmelu.discord.implementation;

import cz.salmelu.discord.SubscriptionManager;
import cz.salmelu.discord.resources.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SubscriptionManagerImpl implements SubscriptionManager {

    private final SubscriptionMaster subscriptionMaster;
    private final ArrayList<String> channelStorage;
    private final HashMap<String, ArrayList<String>> subsStorage;

    public SubscriptionManagerImpl(SubscriptionMaster master, ArrayList<String> channelStorage,
                                   HashMap<String, ArrayList<String>> subsStorage) {
        this.subscriptionMaster = master;
        this.channelStorage = channelStorage;
        this.subsStorage = subsStorage;
    }

    @Override
    public void registerChannel(Channel channel) {
        if(channel.isPrivate()) {
            throw new RuntimeException("Cannot subscribe a private channel.");
        }

        channelStorage.add(channel.getId());
        subsStorage.put(channel.getId(), new ArrayList<>());
        subscriptionMaster.saveStorages();
    }

    @Override
    public void unregisterChannel(Channel channel) {
        channelStorage.remove(channel.getId());
        subsStorage.remove(channel.getId());
        subscriptionMaster.saveStorages();
    }

    @Override
    public void addSubscriber(Message message, String errorMessage) {
        if(!channelStorage.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final ArrayList<String> subs = subsStorage.get(message.getChannel().getId());
        if(subs.contains(message.getAuthor().getId())) {
            message.reply("You are already subscribed.");
        }
        else {
            subs.add(message.getAuthor().getId());
            message.reply("You have been successfully subscribed.");
        }
        subscriptionMaster.saveStorages();
    }

    @Override
    public void removeSubscriber(Message message, String errorMessage) {
        if(!channelStorage.contains(message.getChannel().getId())) {
            message.reply(errorMessage);
            return;
        }

        final ArrayList<String> subs = subsStorage.get(message.getChannel().getId());
        if(subs == null || !subs.contains(message.getAuthor().getId())) {
            message.reply("You are already unsubscribed.");
        }
        else {
            subs.remove(message.getAuthor().getId());
            message.reply("You have been successfully unsubscribed.");
        }
        subscriptionMaster.saveStorages();
    }

    @Override
    public String getSubscribers(Channel channel) throws RuntimeException {
        if(!channelStorage.contains(channel.getId())) {
            throw new RuntimeException("This channel is not subscribed.");
        }

        final ArrayList<String> subs = subsStorage.get(channel.getId());

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
        subscriptionMaster.saveStorages();
    }
}
