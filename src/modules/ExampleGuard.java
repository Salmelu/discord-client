package cz.salmelu.discord.modules;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.PermissionDeniedException;
import cz.salmelu.discord.PermissionGuard;
import cz.salmelu.discord.RequestResponse;
import cz.salmelu.discord.listeners.Initializer;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleGuard implements MessageListener, Initializer {

    private final Context context;
    private final Logger logger = LoggerFactory.getLogger("ChannelCreator");

    public ExampleGuard(Context context) {
        this.context = context;
        PermissionGuard guard = context.getPermissionGuard();
        guard.initialize(0);
        guard.allowPrivateMessages(0);
    }

    @Override
    public void onServerDetected(Server server) {
        final Role role = server.getRoleByName("Admin");
        if(role != null) {
            context.getPermissionGuard().addException(role, 500);
        }
    }

    @Override
    public boolean matchMessage(Message message) {
        return message.getText().startsWith("+channel ");
    }

    @Override
    public void onMessage(Message message) {
        if(!context.getPermissionGuard().isAllowed(message, 300)) {
            message.reply("You cannot do this!");
        }
        else {
            final String channelName = message.getText().trim().substring(9);
            try {
                RequestResponse response = message.getChannel().toServerChannel()
                        .getServer().createTextChannel(channelName, null).get();
                logger.info("Response: " + response.getStatusCode() + " (" + response.getStatusMessage() + ").");
                message.reply("Channel created.");
            }
            catch(PermissionDeniedException e) {
                message.reply("The bot doesn't have permissions to create channels here.");
            }
            catch(Exception e) {
                message.reply("Couldn't create channel.");
            }
        }
    }

    @Override
    public String getName() {
        return "guard";
    }
}
