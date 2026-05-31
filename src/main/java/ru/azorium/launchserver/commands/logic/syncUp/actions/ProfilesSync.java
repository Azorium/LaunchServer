package ru.azorium.launchserver.commands.logic.syncUp.actions;

import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.ZipUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProfilesSync {

    public static void start() {
        List<Path> filesList = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(LauncherApplication.profilesPath)) {
            stream
                    .filter(file -> file.toString().endsWith(".json"))
                    .forEach(filesList::add);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ZipUtils.createZipFromFilesWithoutEnclosure(LauncherApplication.profilesPath, "profiles.zip", filesList);
    }

}
