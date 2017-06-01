package cz.salmelu.discord.modules;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.Storage;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.Message;

import java.time.LocalDateTime;

public class DelayCopyMessage implements MessageListener {
    private final Context context;
    private final Storage storage;

    public DelayCopyMessage(Context context) {
        this.context = context;
        this.storage = context.getStorageManager().getStorage(this, "copier");
        if (!storage.hasValue("last"))
            storage.setValue("last", "none");
    }

    @Override
    public boolean matchMessage(Message message) {
        return message.getRawText().startsWith("+");
    }

    @Override
    public void onMessage(Message message) {
        String last = storage.getValue("last");
        storage.setValue("last", message.getRawText());

        context.getNotifyManager().addNotification(last, o -> {
            message.getChannel().sendMessage((String) o);
        }, LocalDateTime.now().plusSeconds(10));
    }
}
