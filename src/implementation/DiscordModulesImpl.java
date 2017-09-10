package cz.salmelu.discord.implementation;

import cz.salmelu.discord.implementation.resources.ClientImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Main class loading the properties and settings and initializing all the required objects.
 */
public class DiscordModulesImpl {

    private ContextImpl context;
    private Dispatcher dispatcher;
    private ModuleManager manager;
    private ClientImpl client;

    private boolean started = false;

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            started = false;
            client.logout();
            context.getStorageManagerImpl().stop();
            context.getStorageManagerImpl().saveAll();
        }
    }

    public DiscordModulesImpl() {

        try {
            Properties properties = new Properties();
            final InputStream is = cz.salmelu.discord.implementation.DiscordModulesImpl.class.getResourceAsStream("/discord.properties");
            if(is == null) {
                throw new IOException("Couldn't find discord property file.");
            }
            properties.load(is);

            final String token = properties.getProperty("token");
            if(token == null) {
                throw new IOException("Token is not set in properties file.");
            }

            final boolean ignoreBot = Boolean.parseBoolean(properties.getProperty("ignoreBot", "true"));
            final boolean ignoreSelf = Boolean.parseBoolean(properties.getProperty("ignoreSelf", "true"));
            final String helpCommand = properties.getProperty("helpCommand", "");
            final String storagePath = properties.getProperty("storagePath", "./storage");

            context = new ContextImpl(storagePath);
            client = new ClientImpl(token);
            manager = new ModuleManager(context);
            dispatcher = new Dispatcher(client, manager, helpCommand);
            dispatcher.ignoreBotMessages(ignoreBot);
            dispatcher.ignoreOwnMessages(ignoreSelf);

            final String classFiles = properties.getProperty("modules");
            if(classFiles == null || classFiles.equals("")) {
                throw new IOException("You must set up the files with modules to load.");
            }

            final List<String> moduleNames = new ArrayList<>();
            for (String classFile : classFiles.split(",")) {
                final InputStream resource = getClass().getResourceAsStream("/" + classFile);
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
                    moduleNames.addAll(reader.lines()
                            .filter(l -> !(l.matches("\\s*") || l.startsWith("#")))
                            .collect(Collectors.toList()));
                }
            }

            manager.loadModules(moduleNames);

            client.login(dispatcher);
            context.setDispatcher(dispatcher);
            context.startNotifyManager();

            Runtime.getRuntime().addShutdownHook(new ShutdownHook());

            started = true;
        }
        catch(IOException e) {
            System.err.println("Initializing threw an exception.");
            e.printStackTrace();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    public boolean isStarted() {
        return started;
    }
}
