package cz.salmelu.discord.implementation;

import cz.salmelu.discord.implementation.resources.ClientImpl;
import cz.salmelu.discord.modules.*;

public class DiscordClient {

    private final ContextImpl context;
    private final Dispatcher dispatcher;
    private final ModuleManager manager;
    private final ClientImpl client;

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            context.getStorageManagerImpl().saveAll();
            client.logout();
        }
    }

    public DiscordClient() {
        final String helpCommand = "+help";

        context = new ContextImpl();
        client = new ClientImpl(Secret.token);
        manager = new ModuleManager(context);
        dispatcher = new Dispatcher(client, manager, helpCommand);
        dispatcher.ignoreBotMessages(true);
        client.login(dispatcher);

        context.setDispatcher(dispatcher);
        context.startNotifyManager();

        //manager.addModule(CopyMessage.class);
        //manager.addModule(DelayCopyMessage.class);
        manager.addModule(Roller.class);
        manager.addModule(CitadelWatcher.class);
        manager.addModule(InstanceTracker.class);
        manager.addModule(Potatoer.class);
        manager.addModule(Magnuser.class);
        manager.addModule(ClanTop.class);
        manager.addModule(Gainzer.class);
        manager.addModule(Blamer.class);
        manager.addModule(Jaimer.class);
        manager.addModule(Saurer.class);
        manager.addModule(Raids.class);

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public static void main(String[] args) {
        new DiscordClient();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
