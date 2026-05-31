package ru.azorium.launchserver.utils.hash;

import com.google.gson.JsonObject;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.JsonHelper;
import ru.azorium.launchserver.utils.ZipUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Hashes {
    static Path clientsHashesFilePath = LauncherApplication.hashesPath.resolve("clients.json");
    static Path assetsHashesFilePath = LauncherApplication.hashesPath.resolve("assets.json");
    public static Path launcherHashesFilePath = LauncherApplication.hashesPath.resolve("launcher.json");

    public static void createClientsManifest(String folderName) {
        JsonObject clientsObj = getJsonObj(clientsHashesFilePath);
        List<Path> filesList = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(LauncherApplication.clientsPath, 1)) {
            stream
                    .filter(path -> clientsFilter(path, folderName))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        clientsObj.add(fileName, new JsonObject());

                        try (Stream<Path> clientStream = Files.walk(path)) {
                            clientStream
                                    .filter(pathFromClient -> !pathFromClient.toFile().isDirectory())
                                    .forEach(pathFromClient -> {
                                        filesList.add(pathFromClient);
                                        clientsObj.get(fileName).getAsJsonObject()
                                                .addProperty(pathFromClient.toString().replace(path + File.separator, "").replace(File.separator, "/"), Md5.getFileMd5(pathFromClient));
                                    });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        ZipUtils.createZipFromFilesWithEnclosure(LauncherApplication.clientsCachesPath, path, fileName + ".zip", filesList);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        saveObjectToFile(clientsHashesFilePath, clientsObj);
    }

    public static void createAssetsManifest(String version) {
        JsonObject assetsObj = getJsonObj(assetsHashesFilePath);
        Path indexesPath = LauncherApplication.assetsPath.resolve("indexes");

        try (Stream<Path> stream = Files.walk(indexesPath)) {
            stream
                    .skip(1)
                    .filter(path -> assetsFilter(path, version))
                    .forEach(path -> {
                        List<Path> assetFilesList = new ArrayList<>();
                        assetFilesList.add(path);

                        JsonObject versionAssetsObj;
                        String versionElName = path.getFileName().toString();

                        assetsObj.add(versionElName, new JsonObject());

                        try (InputStream is = new URL(path.toUri().toURL().toString()).openStream()) {
                            versionAssetsObj = GlobalObjects.gson.fromJson(new InputStreamReader(is), JsonObject.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        JsonHelper.getObject(versionAssetsObj, "objects").asMap().forEach((key, value) -> {
                            String hash = value.getAsJsonObject().get("hash").getAsString();
                            String substring = hash.substring(0, 2);

                            Path assetPath = LauncherApplication.assetsPath.resolve("objects").resolve(substring + File.separator + hash);

                            if (!Files.exists(assetPath)) return;

                            assetsObj.get(versionElName).getAsJsonObject().addProperty(
                                    substring + "/" + hash,
                                    Md5.getFileMd5(assetPath)
                            );

                            assetFilesList.add(assetPath);
                        });

                        String fileName = path.toString().replace(path.getParent().toString() + File.separator, "").replace(".json", "") + ".zip";
                        ZipUtils.createZipFromFilesWithEnclosure(LauncherApplication.assetsCachesPath, LauncherApplication.assetsPath, fileName, assetFilesList);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        saveObjectToFile(assetsHashesFilePath, assetsObj);
    }

    public static void createLauncherManifest() {
        JsonObject manifestObj = getJsonObj(launcherHashesFilePath);

        try (Stream<Path> stream = Files.walk(LauncherApplication.launcherPath)) {
            stream
                    .skip(1)
                    .forEach(path -> {
                        String fileNameWithoutExtension = path.toString().replace(path.getParent() + File.separator, "").split("\\.")[0];
                        ZipUtils.createZipFromFilesWithoutEnclosure(LauncherApplication.launcherCachesPath, fileNameWithoutExtension + ".zip", new ArrayList<>(List.of(path)));
                        String fileHash = Md5.getFileMd5(path);

                        manifestObj.addProperty(fileNameWithoutExtension, fileHash);
                        LauncherApplication.launcherHashes.put(fileNameWithoutExtension, fileHash);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        saveObjectToFile(launcherHashesFilePath, manifestObj);
    }

    public static JsonObject getJsonObj(Path manifestPath) {
        if (!Files.exists(manifestPath)) {
            return new JsonObject();
        } else {
            try (InputStream is = manifestPath.toUri().toURL().openStream()) {
                return GlobalObjects.gson.fromJson(new InputStreamReader(is), JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static boolean clientsFilter(Path path, String value) {
        if (value.equals("all")) {
            return !path.getFileName().toString().equals("assets") && !path.getFileName().toString().equals("clients");
        } else {
            return path.getFileName().toString().equals(value);
        }
    }

    static boolean assetsFilter(Path path, String version) {
        if (version.equals("all")) {
            return true;
        } else {
            return path.getFileName().toString().equals(version + ".json");
        }
    }

    static void saveObjectToFile(Path filePath, JsonObject obj) {
        try (FileWriter writer = new FileWriter(filePath.toString())) {
            writer.write(GlobalObjects.gson.toJson(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}