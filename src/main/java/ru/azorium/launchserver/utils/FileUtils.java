package ru.azorium.launchserver.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class FileUtils {

    public static void downloadFileFromURL(String url, Path pathToSave) {
        try (InputStream is = new URL(url).openStream()) {
            Files.copy(is, pathToSave, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path createDirectories(Path directory) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return directory;
    }

}
