package cz.salmelu.discord.implementation.json.reflector;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Serializes an instance of a Java object into a {@link JSONObject}.
 */
class Writer {

    private final Serializer serializer;

    Writer(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Writes a single field into the serializer.
     * @param written object being written
     * @param json resulting json object
     * @param fieldName name of written field
     * @param method getter for the field
     */
    void writeField(Object written, JSONObject json, String fieldName, Method method) {
        final Class<?> type = method.getReturnType();
        try {
            if(type.isArray()) {
                // we have an array, we need to make our own list and concatenate them
                final Object[] results = (Object[]) method.invoke(written);
                if(results != null) {
                    final Collection<Object> strings = Arrays.stream(results).map(o
                            -> writeSingleField(written, type.getComponentType())).collect(Collectors.toList());
                    json.put(fieldName, strings);
                }
            }
            else {
                // It's a single object, process
                final Object result = method.invoke(written);
                final Object converted = writeSingleField(result, type);
                json.put(fieldName, converted);
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts an object into another object, that is writable by JSONObject.
     * @param written object being converted
     * @param type type of converted object
     * @return converted object
     */
    private Object writeSingleField(Object written, Class<?> type) {
        if(written == null) {
            return JSONObject.NULL;
        }
        if(type.equals(Integer.class) || type.equals(int.class)) {
            return written;
        }
        else if(type.equals(Long.class) || type.equals(long.class)) {
            return written;
        }
        else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
            return written;
        }
        else if(type.equals(String.class)) {
            return written;
        }
        else if(type.isEnum()) {
            // Handle with caution, we may have a different name
            try {
                MappedName annotation = type.getField(written.toString()).getAnnotation(MappedName.class);
                if(annotation != null) {
                    return annotation.value();
                }
                else {
                    return written.toString();
                }
            }
            catch (NoSuchFieldException e) {
                return written.toString();
            }
        }
        else if(type.equals(OffsetDateTime.class)) {
            return written.toString();
        }
        else if(MappedObject.class.isAssignableFrom(type)) {
            // Another MappedObject inside, recursion
            return serializer.serialize(written);
        }
        else {
            throw new IllegalArgumentException("Invalid type " + type.getName() + " encountered.");
        }
    }

}
