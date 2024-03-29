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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles loading and storing all modules specified in configuration files.
 */
class ModuleManager {

    private final HashMap<Class<?>, Object> modules = new HashMap<>();
    private final List<Initializer> initializers = new ArrayList<>();
    private final List<MessageListener> messageListeners = new ArrayList<>();
    private final HashMap<String, MessageListener> messageListenersByName = new HashMap<>();
    private final List<UserActionListener> actionListeners = new ArrayList<>();
    private final List<ServerListener> serverListeners = new ArrayList<>();
    private final ContextImpl context;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    ModuleManager(ContextImpl context) {
        this.context = context;
        this.context.setModuleManager(this);
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

        final List<Class<?>> interfaces = Arrays.asList(moduleClass.getInterfaces());

        if(interfaces.contains(Initializer.class)) {
            initializers.add((Initializer) module);
        }
        if(interfaces.contains(MessageListener.class)) {
            final MessageListener ml = (MessageListener) module;
            messageListeners.add(ml);
            if(ml.getName() != null) messageListenersByName.put(ml.getName(), ml);
        }
        if (interfaces.contains(UserActionListener.class)) {
            actionListeners.add((UserActionListener) module);
        }
        if (interfaces.contains(ServerListener.class)) {
            serverListeners.add((ServerListener) module);
        }

        modules.put(moduleClass, module);
        logger.info("Module " + moduleClass.getCanonicalName() + " loaded.");
        messageListeners.sort(Comparator.comparingInt(MessageListener::getPriority));
    }

    void loadModules(List<String> moduleList) throws IllegalArgumentException {
        boolean failing = false;
        for (String className : moduleList) {
            try {
                final Class<?> loaded = ClassLoader.getSystemClassLoader().loadClass(className);
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

    @SuppressWarnings("unchecked")
    <T> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    String generateCommandList() {
        final String commandList = messageListeners.stream().filter(MessageListener::isVisibleInHelp)
                .map(MessageListener::getName).collect(Collectors.joining(", "));
        return "```\nAvailable commands: " + commandList + "\n```\n"
                + "For details about a specific command, ask for help with the command name.";
    }
}
