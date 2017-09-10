package cz.salmelu.discord.modules;

import cz.salmelu.discord.Emoji;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.Message;

import java.util.concurrent.ExecutionException;

public class ExampleReaction implements MessageListener {
    @Override
    public boolean matchMessage(Message message) {
        return message.getText().equals("+smile");
    }

    @Override
    public void onMessage(Message message) {
        try {
            message.addReaction(Emoji.SMILE).get();
        }
        catch (InterruptedException | ExecutionException e) {
            // We failed
        }
    }

    @Override
    public String getName() {
        return "smile";
    }

    @Override
    public String getDescription() {
        return "Type +smile to have the bot give you a smile. :-)";
    }

    @Override
    public boolean isVisibleInHelp() {
        return true;
    }
}
