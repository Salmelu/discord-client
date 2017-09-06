package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.listeners.Initializer;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.listeners.ServerListener;
import cz.salmelu.discord.listeners.UserActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles loading and storing all modules specified in configuration files.
 */
class ModuleManager {

    private final List<Initializer> initializers = new ArrayList<>();
    private final List<MessageListener> messageListeners = new ArrayList<>();
    private final HashMap<String, MessageListener> messageListenersByName = new HashMap<>();
    private final List<UserActionListener> actionListeners = new ArrayList<>();
    private final List<ServerListener> serverListeners = new ArrayList<>();
    private final ContextImpl context;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    ModuleManager(ContextImpl context) {
        this.context = context;
        logger.debug("Initialized.");
    }

    private void addModule(Class<?> moduleClass) throws IllegalArgumentException {
        Object module;
        Constructor<?> constructor = null;

        logger.debug("Processing class " + moduleClass.getName() + ".");

        try {
            constructor = moduleClass.getConstructor(Context.class);
            logger.debug("Found Context constructor.");
        }
        catch (NoSuchMethodException e) {
            logger.debug("Context constructor not found, using default constructor.");
        }
        try {
            if (constructor != null) {
                module = constructor.newInstance(context.spawn(moduleClass));
            }
            else {
                module = moduleClass.newInstance();
            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Couldn't initialize an instance of given module.", e);
            throw new IllegalArgumentException("Invalid class supplied as a module.");
        }

        if(Initializer.class.isAssignableFrom(moduleClass)) {
            initializers.add((Initializer) module);
        }
        if(MessageListener.class.isAssignableFrom(moduleClass)) {
            final MessageListener ml = (MessageListener) module;
            messageListeners.add(ml);
            if(ml.getName() != null) messageListenersByName.put(ml.getName(), ml);
        }
        if (UserActionListener.class.isAssignableFrom(moduleClass)) {
            actionListeners.add((UserActionListener) module);
        }
        if (ServerListener.class.isAssignableFrom(moduleClass)) {
            serverListeners.add((ServerListener) module);
        }

        logger.info("Module " + moduleClass.getCanonicalName() + " loaded.");
        messageListeners.sort(Comparator.comparingInt(MessageListener::getPriority));
    }

    void loadModules(List<String> moduleList) throws IllegalArgumentException {
        boolean failing = false;
        for (String className : moduleList) {
            try {
                Class<?> loaded = Class.forName(className);
                addModule(loaded);
            }
            catch (ClassNotFoundException e) {
                System.err.println("Could not find class " + className);
                failing = true;
                break;
            }
        }

        if(failing) {
            throw new IllegalArgumentException("Some of the classes couldn't be loaded.");
        }
    }

    List<Initializer> getInitializers() {
        return initializers;
    }

    List<MessageListener> getMessageListeners() {
        return messageListeners;
    }

    MessageListener getMessageListener(String name) {
        return messageListenersByName.get(name);
    }

    List<UserActionListener> getUserActionListeners() {
        return actionListeners;
    }

    List<ServerListener> getServerListeners() {
        return serverListeners;
    }

    String generateCommandList() {
        final String commandList = messageListeners.stream().filter(MessageListener::isVisibleInHelp)
                .map(MessageListener::getName).collect(Collectors.joining(", "));
        return "```\nAvailable commands: " + commandList + "\n```\n"
                + "For details about a specific command, ask for help with the command name.";
    }
}
