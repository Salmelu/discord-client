package cz.salmelu.discord.implementation;

import cz.salmelu.discord.implementation.resources.ClientImpl;
import cz.salmelu.discord.modules.CopyMessage;
import cz.salmelu.discord.modules.DelayCopyMessage;

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
        context = new ContextImpl();
        client = new ClientImpl(Secret.token);
        manager = new ModuleManager(context);
        dispatcher = new Dispatcher(client, manager);
        dispatcher.ignoreBotMessages(true);
        client.login(dispatcher);

        context.setDispatcher(dispatcher);
        context.startNotifyManager();

        manager.addModule(CopyMessage.class);
        manager.addModule(DelayCopyMessage.class);

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
