package ru.azorium.launchserver;

import com.google.gson.JsonObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.azorium.launchserver.utils.gson.JsonHelper;
import ru.azorium.launchserver.utils.hash.Hashes;
import ru.azorium.launchserver.utils.hash.Md5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootApplication
public class LauncherApplication {

    public static Path rootPath;
    public static Path clientsPath;
    public static Path javaPath;
    public static Path assetsPath;
    public static Path objectsPath;
    public static Path profilesPath;
    public static Path hashesPath;
    public static Path cachesPath;
    public static Path clientsCachesPath;
    public static Path assetsCachesPath;
    public static Path tmpPath;
    public static Path updatesPath;
    public static Path launcherPath;
    public static Path launcherComponentsPath;
    public static Path launcherCachesPath;
    public static Path runtimeZipPath;

    public static Map<String, String> launcherHashes = new HashMap<>();
    public static String runtimeZipHash;

    public static void main(String[] args) throws IOException {
        rootPath = Path.of(System.getProperty("user.dir"));

        profilesPath = rootPath.resolve("profiles");
        hashesPath = rootPath.resolve("hashes");
        cachesPath = rootPath.resolve("caches");
        clientsCachesPath = cachesPath.resolve("clients");
        assetsCachesPath = cachesPath.resolve("assets");
        launcherCachesPath = cachesPath.resolve("launcher");
        tmpPath = rootPath.resolve("tmp");
        updatesPath = rootPath.resolve("updates");
        clientsPath = updatesPath.resolve("clients");
        assetsPath = clientsPath.resolve("assets");
        objectsPath = assetsPath.resolve("objects");
        javaPath = updatesPath.resolve("java");
        launcherPath = updatesPath.resolve("launcher");
        launcherComponentsPath = launcherPath.resolve("components");
        runtimeZipPath = launcherComponentsPath.resolve("runtime_dist.zip");

        createDirectories();
        setLauncherHashes();

        try (Stream<Path> stream = Files.walk(tmpPath)) {
            stream
                    .skip(1)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        SpringApplication.run(LauncherApplication.class, args);
    }

    private static void createDirectories() {
        try {
            Path[] paths = new Path[]{rootPath, clientsPath, javaPath,
                    profilesPath, assetsPath, hashesPath, cachesPath,
                    clientsCachesPath, assetsCachesPath, tmpPath,
                    updatesPath, launcherPath, launcherCachesPath, launcherComponentsPath};
            for (Path path : paths) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setLauncherHashes() {
        try {
            if (!Files.exists(runtimeZipPath)) return;

            runtimeZipHash = Md5.getFileMd5(runtimeZipPath);
            JsonObject launcherHashesObj = JsonHelper.getObjectFromPath(Hashes.launcherHashesFilePath);

            if (!launcherHashesObj.isEmpty()) {
                launcherHashesObj.asMap().forEach((name, el) -> {
                    launcherHashes.put(name, el.getAsString());
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
