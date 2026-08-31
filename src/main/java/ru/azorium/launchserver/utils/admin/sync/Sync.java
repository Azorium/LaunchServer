package ru.azorium.launchserver.utils.admin.sync;

import org.springframework.stereotype.Component;
import ru.azorium.launchserver.config.AppHashes;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.ZipUtils;
import ru.azorium.launchserver.utils.hash.HashManifest;
import ru.azorium.launchserver.utils.hash.Md5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class Sync {
    private final HashManifest hashManifest;
    private final AppPaths appPaths;
    private final AppHashes appHashes;

    public Sync(HashManifest hashManifest, AppPaths appPaths, AppHashes appHashes) {
        this.hashManifest = hashManifest;
        this.appPaths = appPaths;
        this.appHashes = appHashes;
    }

    public void start(SyncContext context) {
        String value = context.value();

        switch (context.action()) {
            case "profiles" -> profiles();
            case "client" -> client(value);
            case "asset" -> asset(value);
            case "launcher" -> launcher();
            case "all" -> {
                profiles();
                client(value);
                asset(value);
                launcher();
            }
        }

        if (context.eventLogger() != null) {
            context.eventLogger().complete();
        }
    }

    private void profiles() {
        Path profilesPath = appPaths.getProfilesPath();
        List<Path> filesList = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(profilesPath)) {
            stream
                    .filter(file -> file.toString().endsWith(".json"))
                    .forEach(filesList::add);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ZipUtils.createZipFromFilesWithoutEnclosure(profilesPath, "profiles.zip", filesList);
    }

    private void client(String clientName) {
        hashManifest.createClientManifest(clientName);
    }

    private void asset(String mcVersion) {
        hashManifest.createAssetsManifest(mcVersion);
    }

    private void launcher() {
        appHashes.setRuntimeZipHash(Md5.getFileMd5(appPaths.getRuntimeZipPath()));
        hashManifest.createLauncherManifest();
    }

}