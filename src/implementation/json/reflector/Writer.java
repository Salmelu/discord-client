package cz.salmelu.discord.implementation.json.reflector;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

class Writer {

    private final Serializer serializer;

    Writer(Serializer serializer) {
        this.serializer = serializer;
    }

    void writeField(Object written, JSONObject json, String fieldName, Method method) {
        final Class<?> type = method.getReturnType();
        try {
            if(type.isArray()) {
                final Object[] results = (Object[]) method.invoke(written);
                final Collection<Object> strings = Arrays.stream(results).map(o
                        -> writeSingleField(written, type.getComponentType())).collect(Collectors.toList());
                json.put(fieldName, strings);
            }
            else {
                final Object result = method.invoke(written);
                final Object converted = writeSingleField(result, type);
                json.put(fieldName, converted);
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

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
        else if(type.equals(LocalDateTime.class)) {
            return written.toString();
        }
        else if(MappedObject.class.isAssignableFrom(type)) {
            return serializer.serialize(written);
        }
        else {
            throw new IllegalArgumentException("Invalid type " + type.getName() + " encountered.");
        }
    }

}
