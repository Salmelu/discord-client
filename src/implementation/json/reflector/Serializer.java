package cz.salmelu.discord.implementation.json.reflector;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Serializer {

    private final Reader reader;
    private final Writer writer;

    public Serializer() {
        reader = new Reader(this);
        writer = new Writer(this);
    }

    public JSONObject serialize(Object object) {
        final JSONObject json = new JSONObject();
        final Class<?> clazz = object.getClass();

        if(!MappedObject.class.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("Cannot serialize class that is not an instance of MappedObject.");
        }

        for(Method method : clazz.getDeclaredMethods()) {
            final String methodName = method.getName();
            if(!methodName.startsWith("get") && !methodName.startsWith("is")) continue;
            final String fieldName = camelToSnake(methodName.substring(methodName.startsWith("is") ? 2 : 3));
            writer.writeField(object, json, fieldName, method);
        }
        return json;
    }

    public <T> T deserialize(JSONObject object, Class<T> clazz) {
        try {
            final T result = clazz.newInstance();
            for(Method method : clazz.getDeclaredMethods()) {
                final String methodName = method.getName();
                if(!methodName.startsWith("set")) continue;
                final String fieldName = camelToSnake(methodName.substring(3));
                if(object.has(fieldName)) {
                    reader.readField(result, method, object.get(fieldName));
                }
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String camelToSnake(String str) {
        StringBuilder builder = new StringBuilder(str.length() + 10);
        builder.append(Character.toLowerCase(str.charAt(0)));
        for(int i = 1; i < str.length(); i++) {
            final char c = str.charAt(i);
            if(Character.isUpperCase(c)) {
                builder.append('_').append(Character.toLowerCase(c));
            }
            else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
