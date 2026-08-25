package ru.azorium.launchserver.utils.update;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.model.dto.ClientProfile;
import ru.azorium.launchserver.utils.gson.JsonHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UpdateFiles {
    private static final Logger log = LoggerFactory.getLogger(UpdateFiles.class);

    public static byte[] updateClient(JsonObject objFromRequest, String date) {
        try {
            String clientFolderName = objFromRequest.get("clientFolderName").getAsString();
            JsonObject clientsObjFromServer = JsonHelper.getObjectFromPath(LauncherApplication.hashesPath.resolve("clients.json"));
            JsonObject serverClientObj = clientsObjFromServer.getAsJsonObject(clientFolderName);
            ClientProfile clientProfile = JsonHelper.objectDeserializerFromPath(LauncherApplication.profilesPath.resolve(clientFolderName + ".json"), ClientProfile.class);

            JsonObject filesObjForArchive = new JsonObject();
            filesObjForArchive.add("toDelete", new JsonArray());
            JsonArray toDeleteJson = filesObjForArchive.get("toDelete").getAsJsonArray();

            List<String> filesFromRequest = new ArrayList<>();
            List<Path> filesToPutArchive = new ArrayList<>();

            Path clientPath = LauncherApplication.clientsPath.resolve(clientFolderName);

            objFromRequest.getAsJsonObject("data").asMap().forEach((key, el) -> {
                filesFromRequest.add(key);
                JsonElement file = serverClientObj.get(key);

                if (file == null) {
                    Stream.of(key)
                            .filter(k -> clientProfile.backend.updateExclusions.stream().noneMatch(k::startsWith))
                            .forEach(toDeleteJson::add);
                    return;
                }

                if (!file.getAsString().equals(el.getAsString())) {
                    toDeleteJson.add(key);
                    filesToPutArchive.add(Path.of(clientPath.resolve(key).toString().replace(LauncherApplication.rootPath + File.separator, "")));
                }
            });

            JsonElement optionalModsObj = objFromRequest.get("optionalMods");
            List<String> disabledOptionalMods = new ArrayList<>();

            if (!optionalModsObj.isJsonNull()) {
                optionalModsObj.getAsJsonArray().forEach(el -> {
                    if (!el.getAsJsonObject().get("value").getAsBoolean()) {
                        disabledOptionalMods.add(el.getAsJsonObject().get("fileName").getAsString());
                    }
                });
            }

            serverClientObj.asMap().forEach((key, el) -> {
                if (!filesFromRequest.contains(key)) {
                    String fileName = Path.of(key).getFileName().toString();
                    if (!disabledOptionalMods.contains(fileName)) {
                        filesToPutArchive.add(Path.of(clientPath.resolve(key).toString().replace(LauncherApplication.rootPath + File.separator, "")));
                    }
                }
            });

            Path jsonFilePath = LauncherApplication.tmpPath.resolve("files-" + date + ".json");
            JsonHelper.saveFromObjToFile(jsonFilePath, filesObjForArchive);
            filesToPutArchive.add(Path.of(jsonFilePath.toString().replace(LauncherApplication.rootPath + File.separator, "")));

            return ByteArrayZip.create(filesToPutArchive);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] updateAssets(JsonObject objFromRequest, String date) {
        try {
            String mcVersion = objFromRequest.get("mcVersion").getAsString();
            JsonObject assetObjFromServer = JsonHelper.getObjectFromPath(LauncherApplication.hashesPath.resolve("assets.json")).get(mcVersion + ".json").getAsJsonObject();
            JsonObject assetObjFromRequest = objFromRequest.get("data").getAsJsonObject();

            JsonObject filesObjForArchive = new JsonObject();
            filesObjForArchive.add("toDelete", new JsonArray());
            JsonArray toDeleteJson = filesObjForArchive.get("toDelete").getAsJsonArray();

            List<String> filesFromRequest = new ArrayList<>();
            List<Path> filesToPutArchive = new ArrayList<>();

            assetObjFromRequest.asMap().forEach((key, el) -> {
                filesFromRequest.add(key);
                JsonElement file = assetObjFromServer.get(key);

                if (file == null) {
                    toDeleteJson.add(key);
                    return;
                }

                if (!file.getAsString().equals(el.getAsString())) {
                    toDeleteJson.add(key);
                    filesToPutArchive.add(Path.of(LauncherApplication.objectsPath.resolve(key).toString().replace(LauncherApplication.rootPath + File.separator, "")));
                }
            });

            assetObjFromServer.asMap().forEach((key, el) -> {
                if (!filesFromRequest.contains(key)) {
                    filesToPutArchive.add(Path.of(LauncherApplication.objectsPath.resolve(key).toString().replace(LauncherApplication.rootPath + File.separator, "")));
                }
            });

            Path jsonFilePath = LauncherApplication.tmpPath.resolve("files-" + date + ".json");
            JsonHelper.saveFromObjToFile(jsonFilePath, filesObjForArchive);
            filesToPutArchive.add(Path.of(jsonFilePath.toString().replace(LauncherApplication.rootPath + File.separator, "")));

            return ByteArrayZip.create(filesToPutArchive);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] updateLauncher(String requestJson) {
        try {
            JsonObject requestObj = JsonParser.parseString(requestJson).getAsJsonObject();
            String requestOSInfo = requestObj.get("osInfo").getAsString();

            Path zipPath = LauncherApplication.launcherCachesPath.resolve(requestOSInfo + ".zip");

            return Files.readAllBytes(zipPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
