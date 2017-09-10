package cz.salmelu.discord.modules;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.Storage;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.Message;

import java.time.LocalDateTime;

public class ExampleStorage implements MessageListener {
    private final Context context;
    private final Storage storage;

    public ExampleStorage(Context context) {
        this.context = context;
        this.storage = context.getStorage("oldies");
        if (!storage.hasValue("last"))
            storage.setValue("last", "none");
    }

    @Override
    public boolean matchMessage(Message message) {
        return message.getRawText().startsWith("+remember ");
    }

    @Override
    public void onMessage(Message message) {
        String last = storage.getValue("last") + " " + message.getAuthor().getMention();
        storage.setValue("last", message.getRawText().substring(10));
        message.getChannel().sendMessage(last);
    }

    @Override
    public String getName() {
        return "remember";
    }

    @Override
    public String getDescription() {
        return "+remember something: remembers your posted message and replies with the last one";
    }

    @Override
    public boolean isVisibleInHelp() {
        return true;
    }
}
