package cz.salmelu.discord.implementation.json.reflector;

import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Converts Java objects into {@link JSONObject} and vice-versa.
 */
public class Serializer {

    /** Read JSON into Object */
    private final Reader reader;
    /** Write Object into JSON */
    private final Writer writer;

    public Serializer() {
        reader = new Reader(this);
        writer = new Writer(this);
    }

    /**
     * Serializes given object into JSON fields and returns created JSON object.
     * @param object object being serialized
     * @return serialized object
     */
    public JSONObject serialize(Object object) {
        final JSONObject json = new JSONObject();
        final Class<?> clazz = object.getClass();

        if(!MappedObject.class.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("Cannot serialize class that is not an instance of MappedObject.");
        }

        // We work with classic Java methods: isXY and getXY
        for(Method method : clazz.getDeclaredMethods()) {
            final String methodName = method.getName();
            if(!methodName.startsWith("get") && !methodName.startsWith("is")) continue;
            // The field name is dependant on method name too
            final String fieldName = camelToSnake(methodName.substring(methodName.startsWith("is") ? 2 : 3));
            writer.writeField(object, json, fieldName, method);
        }
        return json;
    }

    /**
     * Deserializes a JSON object and creates an instance of matching Java object
     * @param object serialized JSON object
     * @param clazz instance of {@link Class} of the expected object
     * @param <T> type of result
     * @return deserialized object
     */
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

    /**
     * Converts Java's camel case into JSON (and Discord's) convention, called snake case.
     * @param str String in CamelCase
     * @return equivalent in snake_case
     */
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
