package cz.salmelu.discord.modules;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.*;

public class CopyMessage implements MessageListener {
    @Override
    public boolean matchMessage(Message message) {
        return message.getText().trim().equals("+emoji");
    }

    @Override
    public void onMessage(Message message) {
        message.addReaction(Emoji.SMILE);
    }

    @Override
    public String getName() {
        return "???";
    }
}
