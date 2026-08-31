package ru.azorium.launchserver.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.azorium.launchserver.LauncherApplication;

import java.nio.file.Path;

@Component
public class AppPaths {
    private Path rootPath;
    private Path clientsPath;
    private Path javaPath;
    private Path assetsPath;
    private Path objectsPath;
    private Path profilesPath;
    private Path hashesPath;
    private Path cachesPath;
    private Path clientsCachesPath;
    private Path assetsCachesPath;
    private Path tmpPath;
    private Path updatesPath;
    private Path launcherPath;
    private Path launcherComponentsPath;
    private Path launcherCachesPath;
    private Path runtimeZipPath;
    private Path launcherHashManifestPath;

    @PostConstruct
    public void init(){
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
        launcherHashManifestPath = hashesPath.resolve("launcher.json");
    }

    public Path getRootPath() {
        return rootPath;
    }

    public Path getClientsPath() {
        return clientsPath;
    }

    public Path getJavaPath() {
        return javaPath;
    }

    public Path getAssetsPath() {
        return assetsPath;
    }

    public Path getObjectsPath() {
        return objectsPath;
    }

    public Path getProfilesPath() {
        return profilesPath;
    }

    public Path getHashesPath() {
        return hashesPath;
    }

    public Path getCachesPath() {
        return cachesPath;
    }

    public Path getClientsCachesPath() {
        return clientsCachesPath;
    }

    public Path getAssetsCachesPath() {
        return assetsCachesPath;
    }

    public Path getTmpPath() {
        return tmpPath;
    }

    public Path getUpdatesPath() {
        return updatesPath;
    }

    public Path getLauncherPath() {
        return launcherPath;
    }

    public Path getLauncherComponentsPath() {
        return launcherComponentsPath;
    }

    public Path getLauncherCachesPath() {
        return launcherCachesPath;
    }

    public Path getRuntimeZipPath() {
        return runtimeZipPath;
    }

    public Path getLauncherHashManifestPath() {
        return launcherHashManifestPath;
    }
}
