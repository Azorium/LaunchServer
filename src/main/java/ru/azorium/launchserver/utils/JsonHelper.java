package ru.azorium.launchserver.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JsonHelper {

    public static String getString(JsonObject obj, String... path) {
        JsonObject current = obj;
        for (int i = 0; i < path.length - 1; i++) {
            if (!current.has(path[i])) return null;
            current = current.getAsJsonObject(path[i]);
        }
        String lastKey = path[path.length - 1];
        return current.has(lastKey) ? current.get(lastKey).getAsString() : null;
    }

    public static int getInt(JsonObject obj, String... path) {
        JsonObject current = obj;
        for (int i = 0; i < path.length - 1; i++) {
            if (!current.has(path[i])) return 0;
            current = current.getAsJsonObject(path[i]);
        }
        String lastKey = path[path.length - 1];
        return current.has(lastKey) ? current.get(lastKey).getAsInt() : 0;
    }

    public static JsonObject getObject(JsonObject obj, String... path) {
        JsonObject current = obj;
        for (String key : path) {
            if (!current.has(key)) return null;
            current = current.getAsJsonObject(key);
        }
        return current;
    }

    public static JsonArray getArray(JsonObject obj, String... path) {
        if (path.length == 0) return null;

        JsonElement current = obj;

        for (int i = 0; i < path.length; i++) {
            String key = path[i];

            if (!(current instanceof JsonObject)) {
                return null;
            }

            JsonObject currentObj = (JsonObject) current;

            if (!currentObj.has(key)) {
                return null;
            }

            if (i == path.length - 1) {
                JsonElement element = currentObj.get(key);
                if (element.isJsonArray()) {
                    return element.getAsJsonArray();
                } else {
                    return null;
                }
            } else {
                JsonElement element = currentObj.get(key);
                if (element.isJsonObject()) {
                    current = element;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public static JsonObject getObjectFromURL(Gson gson, URL url) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
            JsonObject result = gson.fromJson(reader, JsonObject.class);
            if (result == null) {
                throw new IOException("GSON returned null for URL: " + url);
            }
            return result;
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON from " + url + ": " + e.getMessage(), e);
        }
    }
}
