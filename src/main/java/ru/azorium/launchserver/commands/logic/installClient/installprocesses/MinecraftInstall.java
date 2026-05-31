package ru.azorium.launchserver.commands.logic.installClient.installprocesses;

import com.google.gson.*;
import me.tongfei.progressbar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.JsonHelper;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MinecraftInstall {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftInstall.class);
    static String overallMinecraftManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    static String minecraftResourcesUrl = "https://resources.download.minecraft.net";

    private record AssetToDownload(String hash, String subHash, Path path) {
    }

    public String mcVersion;

    public Path clientPath;
    public Path librariesPath;
    public Path versionsPath;
    public boolean isInstalledAssets;

    public MinecraftInstall(String clientName, String mcVersion) {
        this.mcVersion = mcVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
        this.librariesPath = clientPath.resolve("libraries");
        this.versionsPath = clientPath.resolve("versions");
    }

    public boolean start() throws IOException {
        JsonObject versionManifest = getVersionManifest();


        downloadAssets(versionManifest);
        downloadMinecraftClientFile(versionManifest);
        downloadLibraries(versionManifest);

        return isInstalledAssets;
    }

    JsonObject getVersionManifest() throws IOException {
        JsonObject overallMinecraftManifestObj = JsonHelper.getObjectFromURL(GlobalObjects.gson, new URL(overallMinecraftManifestUrl));
        JsonArray versionsArray = JsonHelper.getArray(overallMinecraftManifestObj, "versions");

        AtomicReference<String> versionUrl = new AtomicReference<>();
        versionsArray.asList().stream()
                .filter(v -> Objects.equals(JsonHelper.getString(v.getAsJsonObject(), "id"), mcVersion))
                .forEach(v -> versionUrl.set(JsonHelper.getString(v.getAsJsonObject(), "url")));

        JsonObject versionObject;

        try (InputStream is = new URL(versionUrl.get()).openStream()) {
            byte[] isBytes = is.readAllBytes();
            versionObject = JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(isBytes))).getAsJsonObject();

            FileUtils.createDirectories(versionsPath.resolve(mcVersion));
            Files.copy(new ByteArrayInputStream(isBytes), Path.of(String.format("%s/%s/%s.json", versionsPath, mcVersion, mcVersion)), StandardCopyOption.REPLACE_EXISTING);
        }

        return versionObject;
    }

    void downloadAssets(JsonObject versionJson) throws IOException {
        Path indexesPath = LauncherApplication.assetsPath.resolve("indexes");
        Path objectsPath = LauncherApplication.assetsPath.resolve("objects");
        Path versionManifestPath = indexesPath.resolve(mcVersion + ".json");

        if (Files.exists(versionManifestPath)) {
            isInstalledAssets = true;
            return;
        }

        FileUtils.createDirectories(indexesPath);
        FileUtils.createDirectories(objectsPath);

        URL assetsUrl = new URL(JsonHelper.getString(versionJson, "assetIndex", "url"));
        JsonObject assetsObject = JsonHelper.getObjectFromURL(GlobalObjects.gson, assetsUrl);

        String versionText = GlobalObjects.gson.toJson(assetsObject);
        Files.writeString(versionManifestPath, versionText);

        List<AssetToDownload> toDownload = JsonHelper.getObject(assetsObject, "objects").entrySet().stream()
                .map(entry -> {
                    String hash = JsonHelper.getString(entry.getValue().getAsJsonObject(), "hash");
                    String substring = hash.substring(0, 2);
                    String subHash = substring + "/" + hash;
                    Path assetPath = objectsPath.resolve(subHash);

                    return new AssetToDownload(hash, subHash, assetPath);
                })
                .filter(asset -> !Files.exists(asset.path))
                .toList();

        logger.info("Установка ассетов: ");

        ProgressBarBuilder builder = new ProgressBarBuilder()
                .setStyle(GlobalObjects.pbStyle)
                .setInitialMax(toDownload.size())
                .setUnit(" Asset", 1)
                .setUpdateIntervalMillis(100);

        ProgressBar.wrap(toDownload.parallelStream(), builder)
                .forEach(asset -> {
                    try (InputStream stream = new URL(minecraftResourcesUrl + "/" + asset.subHash).openStream()) {
                        FileUtils.createDirectories(asset.path.getParent());
                        Files.copy(stream, asset.path, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    void downloadMinecraftClientFile(JsonObject versionJson) throws IOException {
        logger.info("Установка файла Minecraft клиента");
        URL fileUrl = new URL(JsonHelper.getString(versionJson, "downloads", "client", "url"));

        try (InputStream is = fileUrl.openStream()) {
            Files.copy(is, Path.of(String.format("%s/%s/%s.jar", versionsPath, mcVersion, mcVersion)), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    void downloadLibraries(JsonObject versionJson) throws IOException {
        logger.info("Установка библиотек: ");
        JsonArray librariesArray = JsonHelper.getArray(versionJson, "libraries");
        FileUtils.createDirectories(librariesPath);

        ProgressBarBuilder builder = new ProgressBarBuilder()
                .setStyle(GlobalObjects.pbStyle)
                .setInitialMax(librariesArray.asList().size())
                .setUnit(" Library", 1)
                .setUpdateIntervalMillis(100);

        ProgressBar.wrap(librariesArray.asList().parallelStream(), builder)
                .forEach(el -> {
                    JsonObject libObject = JsonHelper.getObject(el.getAsJsonObject(), "downloads", "artifact");

                    AtomicReference<Path> libPath = new AtomicReference<>();
                    AtomicReference<URL> libUrl = new AtomicReference<>();

                    if (libObject != null) {
                        libPath.set(Path.of(libObject.get("path").getAsString()));

                        try {
                            libUrl.set(new URL(libObject.get("url").getAsString()));
                            libraryDownload(libPath.get(), libUrl.get());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (JsonHelper.getObject(el.getAsJsonObject(), "downloads", "classifiers") != null) {
                        libObject = JsonHelper.getObject(el.getAsJsonObject(), "downloads", "classifiers");
                        libObject.entrySet().parallelStream().forEach(nativesEl -> {
                            libPath.set(Path.of(nativesEl.getValue().getAsJsonObject().get("path").getAsString()));
                            try {
                                libUrl.set(new URL(nativesEl.getValue().getAsJsonObject().get("url").getAsString()));
                                libraryDownload(libPath.get(), libUrl.get());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                });
    }

    void libraryDownload(Path libPath, URL libUrl) throws IOException {
        if (!Files.exists(librariesPath.resolve(libPath))) {
            Files.createDirectories(librariesPath.resolve(libPath.getParent()));
            try (InputStream is = libUrl.openStream()) {
                Files.copy(is, librariesPath.resolve(libPath), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}