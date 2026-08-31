package ru.azorium.launchserver.service;

import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.azorium.launchserver.config.AppHashes;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.gson.JsonHelper;
import ru.azorium.launchserver.utils.hash.HashManifest;
import ru.azorium.launchserver.utils.hash.Md5;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AppHashesService {
    private final AppHashes appHashes;
    private final AppPaths appPaths;

    public AppHashesService(AppHashes appHashes, AppPaths appPaths) {
        this.appHashes = appHashes;
        this.appPaths = appPaths;
    }

    @PostConstruct
    public void init() {
        setRuntimeZipHash();
        setLauncherHashes();
    }

    private void setRuntimeZipHash() {
        Path runtimeZipPath = appPaths.getRuntimeZipPath();
        if (!Files.exists(runtimeZipPath)) return;

        appHashes.setRuntimeZipHash(Md5.getFileMd5(appPaths.getRuntimeZipPath()));
    }

    private void setLauncherHashes(){
        JsonObject launcherHashesObj = JsonHelper.getObjectFromPath(appPaths.getLauncherHashManifestPath());

        if (!launcherHashesObj.isEmpty()) {
            launcherHashesObj.asMap().forEach((name, el) -> {
                appHashes.getLauncherHashes().put(name, el.getAsString());
            });
        }
    }

}
