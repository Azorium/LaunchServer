package ru.azorium.launchserver.utils.hash;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import ru.azorium.launchserver.config.AppHashes;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.gson.JsonHelper;
import ru.azorium.launchserver.utils.ZipUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class HashManifest {
    private final Path clientsHashManifestPath;
    private final Path assetsHashManifestPath;
    private final AppPaths appPaths;
    private final AppHashes appHashes;

    public HashManifest(AppPaths appPaths, AppHashes appHashes) {
        this.appPaths = appPaths;
        this.clientsHashManifestPath = appPaths.getHashesPath().resolve("clients.json");
        this.assetsHashManifestPath = appPaths.getHashesPath().resolve("assets.json");
        this.appHashes = appHashes;
    }

    public void createClientManifest(String folderName) {
        JsonObject clientsObj = JsonHelper.getObjectFromPath(clientsHashManifestPath);
        List<Path> filesList = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(appPaths.getClientsPath(), 1)) {
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

                        ZipUtils.createZipFromFilesWithEnclosure(appPaths.getClientsCachesPath(), path, fileName + ".zip", filesList);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonHelper.saveFromObjToFile(clientsHashManifestPath, clientsObj);
    }

    public void createAssetsManifest(String version) {
        JsonObject assetsObj = JsonHelper.getObjectFromPath(assetsHashManifestPath);
        Path indexesPath = appPaths.getAssetsPath().resolve("indexes");

        try (Stream<Path> stream = Files.walk(indexesPath)) {
            stream
                    .skip(1)
                    .filter(path -> assetsFilter(path, version))
                    .forEach(path -> {
                        List<Path> assetFilesList = new ArrayList<>();
                        assetFilesList.add(path);

                        JsonObject versionAssetsObj = JsonHelper.getObjectFromPath(path);
                        String versionElName = path.getFileName().toString();

                        assetsObj.add(versionElName, new JsonObject());

                        JsonHelper.getObject(versionAssetsObj, "objects").asMap().forEach((key, value) -> {
                            String hash = value.getAsJsonObject().get("hash").getAsString();
                            String substring = hash.substring(0, 2);

                            Path assetPath = appPaths.getAssetsPath().resolve("objects").resolve(substring + File.separator + hash);

                            if (!Files.exists(assetPath)) return;

                            assetsObj.get(versionElName).getAsJsonObject().addProperty(
                                    substring + "/" + hash,
                                    Md5.getFileMd5(assetPath)
                            );

                            assetFilesList.add(assetPath);
                        });

                        String fileName = path.toString().replace(path.getParent().toString() + File.separator, "").replace(".json", "") + ".zip";
                        ZipUtils.createZipFromFilesWithEnclosure(appPaths.getAssetsCachesPath(), appPaths.getAssetsPath(), fileName, assetFilesList);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonHelper.saveFromObjToFile(assetsHashManifestPath, assetsObj);
    }

    public void createLauncherManifest() {
        JsonObject manifestObj = JsonHelper.getObjectFromPath(appPaths.getLauncherHashManifestPath());

        try (Stream<Path> stream = Files.walk(appPaths.getLauncherPath(), 1)) {
            stream
                    .skip(1)
                    .filter(path -> !path.toFile().isDirectory())
                    .forEach(path -> {
                        String fileNameWithoutExtension = path.toString().replace(path.getParent() + File.separator, "").split("\\.")[0];
                        ZipUtils.createZipFromFilesWithoutEnclosure(appPaths.getLauncherCachesPath(), fileNameWithoutExtension + ".zip", new ArrayList<>(List.of(path)));
                        String fileHash = Md5.getFileMd5(path);

                        manifestObj.addProperty(fileNameWithoutExtension, fileHash);
                        appHashes.getLauncherHashes().put(fileNameWithoutExtension, fileHash);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonHelper.saveFromObjToFile(appPaths.getLauncherHashManifestPath(), manifestObj);
    }

    private boolean clientsFilter(Path path, String value) {
        if (value.equals("all")) {
            return !path.getFileName().toString().equals("assets") && !path.getFileName().toString().equals("clients");
        } else {
            return path.getFileName().toString().equals(value);
        }
    }

    private boolean assetsFilter(Path path, String version) {
        if (version.equals("all")) {
            return true;
        } else {
            return path.getFileName().toString().equals(version + ".json");
        }
    }
}