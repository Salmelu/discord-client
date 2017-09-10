package cz.salmelu.discord.modules;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.Message;

import java.time.OffsetDateTime;

public class ExampleNotification implements MessageListener {
    private final Context context;

    public ExampleNotification(Context context) {
        this.context = context;
    }

    @Override
    public boolean matchMessage(Message message) {
        return message.getRawText().startsWith("+later");
    }

    @Override
    public void onMessage(Message message) {
        String text = message.getRawText();

        context.getNotifyManager().addNotification(text, o -> message.getChannel().sendMessage((String) o),
                OffsetDateTime.now().plusSeconds(7));
    }

    @Override
    public String getName() {
        return "later";
    }

    @Override
    public String getDescription() {
        return "+later something: copies the message back, but with a small delay";
    }

    @Override
    public boolean isVisibleInHelp() {
        return true;
    }
}
