package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Storage;
import cz.salmelu.discord.SubscriptionManager;
import cz.salmelu.discord.resources.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final String S_CHANNELS = "channels";
    private static final String S_SUBS = "subs";
    private final SubscriptionMaster subscriptionMaster;
    private final Storage storage;
    private final ArrayList<String> channels;
    private final Map<String, ArrayList<String>> subs;

    public SubscriptionManagerImpl(SubscriptionMaster master, Storage storage) {
        this.subscriptionMaster = master;
        this.storage = storage;

        if(!storage.hasValue(S_CHANNELS)) {
            storage.setValue(S_CHANNELS, new ArrayList<String>());
        }
        if(!storage.hasValue(S_SUBS)) {
            storage.setValue(S_SUBS, new ArrayList<String>());
        }

        channels = storage.getValue(S_CHANNELS);
        subs = storage.getValue(S_SUBS);
    }

    @Override
    public void registerChannel(Channel channel) {
        if(channel.isPrivate()) {
            throw new RuntimeException("Cannot subscribe a private channel.");
        }

        try {
            storage.lock();
            channels.add(channel.getId());
            subs.put(channel.getId(), new ArrayList<>());
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void unregisterChannel(Channel channel) {
        try {
            storage.lock();
            channels.remove(channel.getId());
            subs.remove(channel.getId());
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void addSubscriber(Message message, String errorMessage) {
        try {
            storage.lock();
            if (!channels.contains(message.getChannel().getId())) {
                message.reply(errorMessage);
                return;
            }

            final ArrayList<String> subArray = subs.get(message.getChannel().getId());
            if (subArray.contains(message.getAuthor().getId())) {
                message.reply("You are already subscribed.");
            }
            else {
                subArray.add(message.getAuthor().getId());
                message.reply("You have been successfully subscribed.");
            }
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public void removeSubscriber(Message message, String errorMessage) {
        try {
            if (!channels.contains(message.getChannel().getId())) {
                message.reply(errorMessage);
                return;
            }

            final ArrayList<String> subArray = subs.get(message.getChannel().getId());
            if (subArray == null || !subArray.contains(message.getAuthor().getId())) {
                message.reply("You are already unsubscribed.");
            }
            else {
                subArray.remove(message.getAuthor().getId());
                message.reply("You have been successfully unsubscribed.");
            }
        }
        finally {
            storage.unlock();
        }
    }

    @Override
    public String getSubscribers(Channel channel) throws RuntimeException {
        try {
            storage.lock();
            if (!channels.contains(channel.getId())) {
                throw new RuntimeException("This channel is not subscribed.");
            }

            final ArrayList<String> subArray = subs.get(channel.getId());

            final Server server = channel.toServerChannel().getServer();
            purgeSubscribers(server, subArray);

            return subArray.stream()
                    .map(id -> server.getMemberById(id).getMention())
                    .collect(Collectors.joining(" "));
        }
        finally {
            storage.unlock();
        }
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
