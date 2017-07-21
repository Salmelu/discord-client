package cz.salmelu.discord.implementation.json.reflector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

class Reader {
    private final Serializer serializer;

    Reader(Serializer serializer) {
        this.serializer = serializer;
    }

    void readField(Object result, Method method, Object value) throws IllegalArgumentException {
        final Class<?> type = method.getParameterTypes()[0];
        try {
            if (value.equals(JSONObject.NULL)) {
                return;
            }
            else if(type.isArray()) {
                final ArrayList<Object> values = new ArrayList<>();
                final JSONArray valueArray = (JSONArray) value;

                if(valueArray.length() == 0) {
                    // Empty array
                    method.invoke(result, Array.newInstance(type.getComponentType(), 0));
                    return;
                }

                valueArray.forEach(v -> values.add(readSingleValue(type.getComponentType(), v)));
                Object[] array = values.toArray((Object[]) Array.newInstance(type.getComponentType(), values.size()));
                method.invoke(result, type.cast(array));
            }
            else {
                method.invoke(result, readSingleValue(type, value));
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object readSingleValue(Class<?> type, Object value) {
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return value;
        }
        else if (type.equals(long.class) || type.equals(Long.class)) {
            return value;
        }
        else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return value;
        }
        else if (type.equals(String.class)) {
            return value;
        }
        else if (type.isEnum()) {
            Optional<?> optional = Arrays.stream(type.getEnumConstants()).filter(en -> {
                try {
                    MappedName annotation = type.getField(en.toString()).getAnnotation(MappedName.class);
                    if(annotation != null) {
                        return value.equals(annotation.value());
                    }
                    else {
                        return value.equals(en.toString());
                    }
                }
                catch (NoSuchFieldException e) {
                    return value.equals(en.toString());
                }
            }).findFirst();
            return optional.orElse(null);
        }
        else if (type.equals(LocalDateTime.class)) {
            return LocalDateTime.parse(value.toString());
        }
        else if (MappedObject.class.isAssignableFrom(type)) {
            return serializer.deserialize((JSONObject) value, type);
        }
        else {
            throw new IllegalArgumentException("The field requested cannot be set as it is an unknown type. " + type.getName());
        }
    }
}