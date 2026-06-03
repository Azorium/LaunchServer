package ru.azorium.launchserver.utils.gson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.dto.starter.Fabric;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.azorium.launchserver.GlobalObjects.gson;

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

    public static JsonObject getObjectFromURL(String url) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            JsonObject result = gson.fromJson(reader, JsonObject.class);
            if (result == null) {
                throw new IOException("GSON returned null for URL: " + url);
            }
            return result;
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON from " + url + ": " + e.getMessage(), e);
        }
    }

    public static JsonObject getObjectFromPath(Path jsonPath) {
        if (!Files.exists(jsonPath)) {
            return new JsonObject();
        } else {
            try (InputStream is = jsonPath.toUri().toURL().openStream()) {
                return GlobalObjects.gson.fromJson(new InputStreamReader(is), JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveFromObjToFile(Path filePath, JsonObject obj){
        try (FileWriter writer = new FileWriter(filePath.toString())) {
            gson.toJson(obj, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T objectDeserializerFromPath(Path path, Class<T> classOfT){
        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, classOfT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
