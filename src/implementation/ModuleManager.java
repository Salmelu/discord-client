package cz.salmelu.discord.implementation;

import cz.salmelu.discord.Context;
import cz.salmelu.discord.listeners.Initializer;
import cz.salmelu.discord.listeners.MessageListener;
import cz.salmelu.discord.listeners.UserActionListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {

    private final List<Initializer> initializers = new ArrayList<>();
    private final List<MessageListener> messageListeners = new ArrayList<>();
    private final List<UserActionListener> actionListeners = new ArrayList<>();
    private final Context context;

    public ModuleManager(Context context) {
        this.context = context;
    }

    public void addModule(Class<?> moduleClass) {
        Object module;
        Constructor<?> constructor = null;
        try {
            constructor = moduleClass.getConstructor(Context.class);
        }
        catch(NoSuchMethodException e) {
            // Not found, okay
        }
        try {
            if(constructor != null) {
                module = constructor.newInstance(context);
            }
            else {
                module = moduleClass.newInstance();
            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Invalid class supplied as a module.");
        }

        if(Initializer.class.isAssignableFrom(moduleClass)) {
            initializers.add((Initializer) module);
        }
        if(MessageListener.class.isAssignableFrom(moduleClass)) {
            messageListeners.add((MessageListener) module);
        }
        if (UserActionListener.class.isAssignableFrom(moduleClass)) {
            actionListeners.add((UserActionListener) module);
        }
        messageListeners.sort(Comparator.comparingInt(MessageListener::getPriority));
    }

    public void loadModules() {
        messageListeners.sort(Comparator.comparingInt(MessageListener::getPriority));
    }

    public List<Initializer> getInitializers() {
        return initializers;
    }

    public List<MessageListener> getMessageListeners() {
        return messageListeners;
    }

    public List<UserActionListener> getUserActionListeners() {
        return actionListeners;
    }
}
