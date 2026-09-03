package ru.azorium.launchserver.utils.admin.installclient.installprocesses;

import com.google.gson.*;
import ru.azorium.launchserver.AppConstants;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.admin.EventLogger;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.gson.JsonHelper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MinecraftInstaller {
    private static final String overallMinecraftManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String minecraftResourcesUrl = "https://resources.download.minecraft.net";
    private final String mcVersion;
    private final Path librariesPath;
    private final Path versionsPath;
    private boolean isInstalledAssets;
    private final InstallContext context;
    private final AppPaths appPaths;

    private record AssetToDownload(String hash, String subHash, Path path) {
    }

    public MinecraftInstaller(InstallContext context, AppPaths appPaths) {
        this.librariesPath = context.librariesPath();
        this.versionsPath = context.versionsPath();
        this.mcVersion = context.mcVersion();
        this.context = context;
        this.appPaths = appPaths;
    }

    public boolean start() throws IOException {
        JsonObject versionManifest = getVersionManifest();
        downloadAssets(versionManifest);
        downloadMinecraftClientFile(versionManifest);
        downloadLibraries(versionManifest);

        return isInstalledAssets;
    }

    private JsonObject getVersionManifest() throws IOException {
        JsonObject overallMinecraftManifestObj = JsonHelper.getObjectFromURL(overallMinecraftManifestUrl);
        JsonArray versionsArray = JsonHelper.getArray(overallMinecraftManifestObj, "versions");

        AtomicReference<String> versionUrl = new AtomicReference<>();
        versionsArray.asList().stream()
                .filter(v -> Objects.equals(JsonHelper.getString(v.getAsJsonObject(), "id"), mcVersion))
                .forEach(v -> versionUrl.set(JsonHelper.getString(v.getAsJsonObject(), "url")));

        JsonObject versionObject = JsonHelper.getObjectFromURL(versionUrl.get());
        FileUtils.createDirectories(versionsPath.resolve(mcVersion));
        JsonHelper.saveFromObjToFile(Path.of(String.format("%s/%s/%s.json", versionsPath, mcVersion, mcVersion)), versionObject);

        return versionObject;
    }

    private void downloadAssets(JsonObject versionJson) throws IOException {
        context.logger().info(new EventLogger.InstallEventMessage(null, "Установка ассетов", "Установка ассетов"));
        Path indexesPath = appPaths.getAssetsPath().resolve("indexes");
        Path objectsPath = appPaths.getAssetsPath().resolve("objects");
        Path versionManifestPath = indexesPath.resolve(mcVersion + ".json");

        if (Files.exists(versionManifestPath)) {
            isInstalledAssets = true;
            return;
        }

        FileUtils.createDirectories(indexesPath);
        FileUtils.createDirectories(objectsPath);

        String assetsUrl = JsonHelper.getString(versionJson, "assetIndex", "url");
        JsonObject assetsObject = JsonHelper.getObjectFromURL(assetsUrl);

        String versionText = AppConstants.gson.toJson(assetsObject);
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

        toDownload.parallelStream().forEach(asset -> {
            FileUtils.createDirectories(asset.path.getParent());
            FileUtils.downloadFileFromURL(minecraftResourcesUrl + "/" + asset.subHash, asset.path);
        });
    }

    private void downloadMinecraftClientFile(JsonObject versionJson) throws IOException {
        context.logger().info(new EventLogger.InstallEventMessage(null, "Установка файла Minecraft клиента", "Установка файла Minecraft клиента"));

        URL fileUrl = new URL(JsonHelper.getString(versionJson, "downloads", "client", "url"));

        try (InputStream is = fileUrl.openStream()) {
            Files.copy(is, Path.of(String.format("%s/%s/%s.jar", versionsPath, mcVersion, mcVersion)), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void downloadLibraries(JsonObject versionJson) {
        context.logger().info(new EventLogger.InstallEventMessage(null, "Установка библиотек", "Установка библиотек"));

        JsonArray librariesArray = JsonHelper.getArray(versionJson, "libraries");
        FileUtils.createDirectories(librariesPath);

        librariesArray.asList().parallelStream().forEach(el -> {
            JsonObject libObject = JsonHelper.getObject(el.getAsJsonObject(), "downloads", "artifact");

            AtomicReference<Path> libPath = new AtomicReference<>();
            AtomicReference<String> libUrl = new AtomicReference<>();

            if (libObject != null) {
                libPath.set(Path.of(libObject.get("path").getAsString()));

                try {
                    libUrl.set(libObject.get("url").getAsString());
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
                        libUrl.set(nativesEl.getValue().getAsJsonObject().get("url").getAsString());
                        libraryDownload(libPath.get(), libUrl.get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    private void libraryDownload(Path libPath, String libUrl) throws IOException {
        if (!Files.exists(librariesPath.resolve(libPath))) {
            Files.createDirectories(librariesPath.resolve(libPath.getParent()));
            FileUtils.downloadFileFromURL(libUrl, librariesPath.resolve(libPath));
        }
    }

}