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

public class ModuleManager {

    private final List<Initializer> initializers = new ArrayList<>();
    private final List<MessageListener> messageListeners = new ArrayList<>();
    private final HashMap<String, MessageListener> messageListenersByName = new HashMap<>();
    private final List<UserActionListener> actionListeners = new ArrayList<>();
    private final List<ServerListener> serverListeners = new ArrayList<>();
    private final ContextImpl context;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public ModuleManager(ContextImpl context) {
        this.context = context;
        logger.debug("Initialized.");
    }

    public void addModule(Class<?> moduleClass) throws IllegalArgumentException {
        Object module = null;
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

        logger.debug("Module initialized.");
        messageListeners.sort(Comparator.comparingInt(MessageListener::getPriority));
    }

    public void loadModules(List<String> moduleList) throws IllegalArgumentException {
        boolean failing = false;
        for (String className : moduleList) {
            try {
                Class<?> loaded = Class.forName(className);
                addModule(loaded);
            }
            catch (ClassNotFoundException e) {
                System.err.println("Could not find class " + className);
                failing = true;
            }
        }

        if(failing) {
            throw new IllegalArgumentException("Some of the classes couldn't been loaded.");
        }
    }

    public List<Initializer> getInitializers() {
        return initializers;
    }

    public List<MessageListener> getMessageListeners() {
        return messageListeners;
    }

    public MessageListener getMessageListener(String name) {
        return messageListenersByName.get(name);
    }

    public List<UserActionListener> getUserActionListeners() {
        return actionListeners;
    }

    public List<ServerListener> getServerListeners() {
        return serverListeners;
    }

    public String generateCommandList() {
        final String commandList = messageListeners.stream().filter(MessageListener::isVisibleInHelp)
                .map(MessageListener::getName).collect(Collectors.joining(", "));
        return "```\nAvailable commands: " + commandList + "\n```\n"
                + "For details about a specific command, ask for help with the command name.";
    }
}
