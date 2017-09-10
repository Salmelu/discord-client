package cz.salmelu.discord.modules;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.*;

public class ExampleReply implements MessageListener {
    @Override
    public boolean matchMessage(Message message) {
        return message.getText().trim().startsWith("+copy ");
    }

    @Override
    public void onMessage(Message message) {
        message.reply(message.getText().substring(6));
    }

    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public String getDescription() {
        return "copies any message starting with \"+copy\", that was posted";
    }

    @Override
    public boolean isVisibleInHelp() {
        return true;
    }
}
