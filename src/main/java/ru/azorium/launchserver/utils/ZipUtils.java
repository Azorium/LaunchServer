package ru.azorium.launchserver.utils;

import com.google.gson.JsonObject;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.jline.builtins.Completers;
import ru.azorium.launchserver.LauncherApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ZipUtils {

    public static void createZipFromFilesWithoutEnclosure(Path saveFolder, String fileName, List<Path> files) {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(saveFolder.resolve(fileName).toFile()))) {
            zipOut.setLevel(5);

            for (Path file : files) {
                String[] filePathParts = file.toString().split(Pattern.quote(File.separator));
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(file.toFile(), filePathParts[filePathParts.length - 1]);

                createAndWriteFile(zipOut, file, zipEntry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createAndWriteFile(ZipArchiveOutputStream zipOut, Path file, ZipArchiveEntry zipEntry) throws IOException {
        zipOut.putArchiveEntry(zipEntry);

        try (InputStream inputStream = Files.newInputStream(file)) {
            byte[] buffer = new byte[1048576];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
        }

        zipOut.closeArchiveEntry();
    }

    public static void createZipFromFilesWithEnclosure(Path saveFolder, Path pathToReplace, String fileName, List<Path> files) {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(saveFolder.resolve(fileName).toFile()))) {
            zipOut.setLevel(5);

            for (Path file : files) {
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(file.toFile(), file.toString().replace(pathToReplace + File.separator, ""));

                createAndWriteFile(zipOut, file, zipEntry);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createZipFromFolder(Path saveFolder, String fileName, Path folderPath) {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(saveFolder.resolve(fileName).toFile()))) {
            zipOut.setLevel(5);

            try (Stream<Path> stream = Files.walk(folderPath)) {
                stream
                        .skip(1)
                        .forEach(path -> {
                            try {
                                File file = path.toFile();
                                String fileNameWithPath = file.toPath().toString().replace(folderPath + File.separator, "");

                                if (file.isDirectory()) {
                                    ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, fileNameWithPath);
                                    zipOut.putArchiveEntry(zipEntry);
                                    zipOut.closeArchiveEntry();
                                    return;
                                }

                                ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, fileNameWithPath);

                                zipOut.putArchiveEntry(zipEntry);

                                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                                    byte[] buffer = new byte[1048576];
                                    int length;
                                    while ((length = inputStream.read(buffer)) > 0) {
                                        zipOut.write(buffer, 0, length);
                                    }
                                }

                                zipOut.closeArchiveEntry();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] removeModsFromClientArchive(JsonObject requestObj) {
        try {
            Path archivePath = LauncherApplication.clientsCachesPath.resolve(requestObj.get("clientfolderName").getAsString() + ".zip");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            List<String> disabledOptionalMods = new ArrayList<>();

            requestObj.get("optionalMods").getAsJsonArray().asList().forEach(el -> {
                boolean value = el.getAsJsonObject().get("value").getAsBoolean();
                if (!value) {
                    disabledOptionalMods.add(el.getAsJsonObject().get("fileName").getAsString());
                }
            });

            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(archivePath));
                 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {

                ZipArchiveEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    if (disabledOptionalMods.stream().anyMatch(entryName::contains)) {
                        continue;
                    }

                    ZipArchiveEntry newEntry = new ZipArchiveEntry(entryName);
                    newEntry.setTime(entry.getTime());

                    zos.putArchiveEntry(newEntry);

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeArchiveEntry();
                }
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
