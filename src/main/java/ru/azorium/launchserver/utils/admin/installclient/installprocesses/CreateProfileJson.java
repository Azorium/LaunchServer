package ru.azorium.launchserver.utils.admin.installclient.installprocesses;

import com.google.gson.JsonObject;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;
import ru.azorium.launchserver.model.dto.ClientProfile;
import ru.azorium.launchserver.utils.gson.JsonHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static ru.azorium.launchserver.AppConstants.gson;

public class CreateProfileJson {
    Path profilePath;
    ClientProfile clientProfileObj;
    int minorNum;
    JsonObject mcManifestObj;
    JsonObject loaderManifestObj;
    private final String clientName;
    private final String mcVersion;
    private final String loaderType;
    private final String loaderVersion;
    private final Path clientPath;
    private final Path versionsPath;
    private AppPaths appPaths;

    public CreateProfileJson(InstallContext context, AppPaths appPaths) {
        this.clientName = context.clientName();
        this.mcVersion = context.mcVersion();
        this.loaderType = context.loaderType();
        this.loaderVersion = context.loaderVersion();
        this.clientPath = context.clientPath();
        this.versionsPath = context.versionsPath();
        this.appPaths = appPaths;
    }

    public void start() throws IOException {
        profilePath = appPaths.getProfilesPath().resolve(clientName + ".json");
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
                    mcManifestObj = gson.fromJson(reader, JsonObject.class);
                } else {
                    loaderManifestObj = gson.fromJson(reader, JsonObject.class);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    void mergeManifests() {
        clientProfileObj = new ClientProfile();
        clientProfileObj.frontend = new ClientProfile.Frontend();
        clientProfileObj.backend = new ClientProfile.Backend();
        clientProfileObj.backend.arguments = new ClientProfile.Arguments();
        clientProfileObj.backend.classpaths = new ClientProfile.Classpaths();
        clientProfileObj.backend.updateExclusions = new ArrayList<>();

        clientProfileObj.id = setProfileId();
        clientProfileObj.mcVersion = mcVersion;
        clientProfileObj.loaderType = loaderType;
        clientProfileObj.loaderVersion = loaderVersion;
        clientProfileObj.optionalMods = new ArrayList<>();

        clientProfileObj.frontend.name = clientName;
        clientProfileObj.frontend.description = "Client description";
        clientProfileObj.frontend.profileImages = new String[]{};
        clientProfileObj.frontend.image = "/assets/images/servers/default.jpg";

        clientProfileObj.backend.ip = "localhost";
        clientProfileObj.backend.port = "25565";
        clientProfileObj.backend.folderName = clientName;
        clientProfileObj.backend.javaVersion = JsonHelper.getString(mcManifestObj, "javaVersion", "majorVersion");
        clientProfileObj.backend.mainClass = getMainClass();

        setClasspaths();
        setGameArguments();
    }

    private String getMainClass() {
        return minorNum < 12 ? JsonHelper.getString(loaderManifestObj, "versionInfo", "mainClass") : JsonHelper.getString(loaderManifestObj, "mainClass");
    }

    private void setClasspaths() {
        try {
            clientProfileObj.backend.classpaths.linux = new ClasspathBuilder(clientPath, mcVersion, gson, loaderVersion, loaderType, "linux").build();
            clientProfileObj.backend.classpaths.macos = new ClasspathBuilder(clientPath, mcVersion, gson, loaderVersion, loaderType, "macos").build();
            clientProfileObj.backend.classpaths.windows = new ClasspathBuilder(clientPath, mcVersion, gson, loaderVersion, loaderType, "windows").build();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        if (minor < 12) {
            clientProfileObj.backend.arguments.game = Collections.singletonList(JsonHelper.getString(loaderManifestObj, "versionInfo", "minecraftArguments"));
        } else if (minor < 16) {
            clientProfileObj.backend.arguments.game = Collections.singletonList(JsonHelper.getString(loaderManifestObj, "minecraftArguments"));
        } else {
            clientProfileObj.backend.arguments.game = new ArrayList<>();
            clientProfileObj.backend.arguments.jvm = new ArrayList<>();

            JsonHelper.getArray(mcManifestObj, "arguments", "game").asList().stream()
                    .filter(el -> {
                        return !el.isJsonObject();
                    })
                    .forEach(el -> clientProfileObj.backend.arguments.game.add(el.getAsString()));
            JsonHelper.getArray(loaderManifestObj, "arguments", "game").asList().forEach(el -> clientProfileObj.backend.arguments.game.add(el.getAsString()));


            JsonHelper.getArray(mcManifestObj, "arguments", "jvm").asList().stream()
                    .filter(el -> {
                        return !el.toString().equals("\"-cp\"") && !el.toString().equals("\"${classpath}\"") && !el.isJsonObject() && !el.toString().contains("library.path");
                    })
                    .forEach(el -> clientProfileObj.backend.arguments.jvm.add(el.getAsString()));
            JsonHelper.getArray(loaderManifestObj, "arguments", "jvm").asList().forEach(el -> clientProfileObj.backend.arguments.jvm.add(el.getAsString()));

        }
    }

    void createProfileFile() {
        List<String> game = clientProfileObj.backend.arguments.game;
        JsonObject jsonObject = gson.toJsonTree(clientProfileObj).getAsJsonObject();

        if (game.size() == 1) {
            jsonObject.getAsJsonObject("backend").getAsJsonObject("arguments").addProperty("game", game.get(0));
        }

        JsonHelper.saveFromObjToFile(profilePath, jsonObject);
    }

}
