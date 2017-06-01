package cz.salmelu.discord.implementation.json;

import com.google.gson.*;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JSONMappedObject {

    private static Gson gson;

    static {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            final JsonSerializer<LocalDateTime> datetimeSerializer =
                    (dateTime, type, jsonSerializationContext) -> new JsonPrimitive(dateTime.format(formatter));
            final JsonDeserializer<LocalDateTime> datetimeDeserializer = (json, type, context) -> {
                if (json == null) return null;
                return LocalDateTime.from(formatter.parse(json.getAsString()));
            };

            gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(LocalDateTime.class, datetimeSerializer)
                    .registerTypeAdapter(LocalDateTime.class, datetimeDeserializer)
                    .create();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject serialize() {
        return new JSONObject(gson.toJson(this));
    }

    public static <T> T deserialize(String object, Class<T> clazz) {
        return gson.fromJson(object, clazz);
    }

    public static <T> T deserialize(JSONObject object, Class<T> clazz) {
        return deserialize(object.toString(), clazz);
    }
}
