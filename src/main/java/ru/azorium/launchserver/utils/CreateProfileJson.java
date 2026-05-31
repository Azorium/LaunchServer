package ru.azorium.launchserver.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.commands.logic.syncUp.SyncUp;
import ru.azorium.launchserver.commands.logic.syncUp.actions.ProfilesSync;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class CreateProfileJson {
    static Path profilePath;
    static JsonObject clientProfileObj = new JsonObject();
    static int minorNum;

    static JsonObject mcManifestObj;
    static JsonObject loaderManifestObj;

    public String clientName;
    public String mcVersion;
    public String loaderType;
    public String loaderVersion;
    public Path clientPath;
    public Path versionsPath;

    public CreateProfileJson(String clientName, String mcVersion, String loaderType, String loaderVersion) {
        this.clientName = clientName;
        this.mcVersion = mcVersion;
        this.loaderType = loaderType;
        this.loaderVersion = loaderVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
        this.versionsPath = clientPath.resolve("versions");
    }

    public void start() throws IOException {
        profilePath = LauncherApplication.profilesPath.resolve(clientName + ".json");
        minorNum = MinecraftVersions.getMinorNum(MinecraftVersions.getParts(mcVersion));
        getManifests();
        mergeManifests();
        createProfileFile();
    }

    void getManifests() throws IOException {
        Map<String, Path> paths = new HashMap<>();

        Files.walk(versionsPath)
                .filter(path -> {
                    return path.toFile().getName().endsWith(".json");
                })
                .forEach(path -> {
                    if (path.toFile().getName().equals(mcVersion + ".json")) {
                        paths.put("mcManifest", path);
                    } else {
                        paths.put("forgeManifest", path);
                    }
                });

        paths.forEach((manifest, path) -> {
            try (InputStreamReader reader = new InputStreamReader(path.toFile().toURL().openStream())) {
                if (manifest.equals("mcManifest")) {
                    mcManifestObj = GlobalObjects.gson.fromJson(reader, JsonObject.class);
                } else {
                    loaderManifestObj = GlobalObjects.gson.fromJson(reader, JsonObject.class);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    void mergeManifests() {
        clientProfileObj.addProperty("id", setProfileId());
        clientProfileObj.addProperty("mcVersion", mcVersion);
        clientProfileObj.addProperty("loaderType", loaderType);
        clientProfileObj.addProperty("loaderVersion", loaderVersion);

        clientProfileObj.add("frontend", new JsonObject());
        clientProfileObj.add("backend", new JsonObject());

        JsonObject frontendJson = clientProfileObj.get("frontend").getAsJsonObject();
        JsonObject backendJson = clientProfileObj.get("backend").getAsJsonObject();

        frontendJson.addProperty("name", clientName);
        frontendJson.addProperty("description", "Client description");
        frontendJson.addProperty("image", "/assets/images/default.jpg");

        backendJson.addProperty("ip", "localhost");
        backendJson.addProperty("port", "25565");
        backendJson.addProperty("folderName", clientName);
        backendJson.addProperty("javaVersion", JsonHelper.getString(mcManifestObj, "javaVersion", "majorVersion"));
        backendJson.addProperty("mainClass", getMainClass());
        backendJson.add("arguments", new JsonObject());
        backendJson.add("classpaths", new JsonObject());
        JsonObject classpathsObj = backendJson.get("classpaths").getAsJsonObject();

        clientProfileObj.add("optionalMods", new JsonArray());

        setClasspaths(classpathsObj);
        setGameArguments();
    }

    private String getMainClass() {
        return minorNum < 12 ? JsonHelper.getString(loaderManifestObj, "versionInfo", "mainClass") : JsonHelper.getString(loaderManifestObj, "mainClass");
    }

    private void setClasspaths(JsonObject classpathsObj) {
        String[] osArray = new String[]{"windows", "linux", "macos"};
        for (String os : osArray) {
            try {
                classpathsObj.addProperty(os, new ClasspathBuilder(clientPath, mcVersion, GlobalObjects.gson, loaderVersion, loaderType, os).build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int setProfileId() {
        AtomicInteger i = new AtomicInteger();
        try (Stream<Path> stream = Files.walk(profilePath.getParent())) {
            stream
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> i.getAndIncrement());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return i.get();
    }

    void setGameArguments() {
        int minor = minorNum;
        String gameArgs;

        JsonObject argumentsJson = clientProfileObj.getAsJsonObject("backend").getAsJsonObject("arguments");

        if (minor < 12) {
            gameArgs = JsonHelper.getString(loaderManifestObj, "versionInfo", "minecraftArguments");
        } else if (minor < 16) {
            gameArgs = JsonHelper.getString(loaderManifestObj, "minecraftArguments");
        } else {
            argumentsJson.add("game", new JsonArray());
            JsonArray gameJson = argumentsJson.getAsJsonArray("game");

            JsonHelper.getArray(mcManifestObj, "arguments", "game").asList().stream()
                    .filter(el -> {
                        return !el.isJsonObject();
                    })
                    .forEach(gameJson::add);
            JsonHelper.getArray(loaderManifestObj, "arguments", "game").asList().forEach(gameJson::add);

            argumentsJson.add("jvm", new JsonArray());
            JsonArray jvmJson = argumentsJson.getAsJsonArray("jvm");
            JsonHelper.getArray(mcManifestObj, "arguments", "jvm").asList().stream()
                    .filter(el -> {
                        return !el.toString().equals("\"-cp\"") && !el.toString().equals("\"${classpath}\"") && !el.isJsonObject() && !el.toString().contains("library.path");
                    })
                    .forEach(jvmJson::add);
            JsonHelper.getArray(loaderManifestObj, "arguments", "jvm").asList().forEach(jvmJson::add);

            return;
        }

        argumentsJson.addProperty("game", gameArgs);
    }

    void createProfileFile() {
        try (FileWriter writer = new FileWriter(profilePath.toString())) {
            GlobalObjects.gson.toJson(clientProfileObj, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
