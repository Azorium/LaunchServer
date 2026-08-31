package ru.azorium.launchserver.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class AppFilesService {
    private final AppPaths appPaths;

    public AppFilesService(AppPaths appPaths) {
        this.appPaths = appPaths;
    }

    @PostConstruct
    public void init() throws IOException {
        appPaths.init();
        createDirs();
        deleteTmpDirFiles();
    }

    private void createDirs(){
        Path[] paths = new Path[]{
                appPaths.getRootPath(), appPaths.getClientsPath(), appPaths.getJavaPath(),
                appPaths.getProfilesPath(), appPaths.getAssetsPath(), appPaths.getHashesPath(),
                appPaths.getCachesPath(), appPaths.getClientsCachesPath(),
                appPaths.getAssetsCachesPath(), appPaths.getTmpPath(),
                appPaths.getUpdatesPath(), appPaths.getLauncherPath(),
                appPaths.getLauncherCachesPath(), appPaths.getLauncherComponentsPath()
        };

        for (Path path : paths) {
            FileUtils.createDirectories(path);
        }
    }

    private void deleteTmpDirFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(appPaths.getTmpPath())) {
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
    }

}
