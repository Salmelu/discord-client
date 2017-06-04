package cz.salmelu.discord.modules;

import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.Message;

public class CopyMessage implements MessageListener {
    @Override
    public boolean matchMessage(Message message) {
        return message.getRawText().startsWith("!");
    }

    @Override
    public void onMessage(Message message) {
        message.getChannel().sendMessage(message.getRawText());
    }

    @Override
    public String getName() {
        return "copy";
    }
}
