package ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.gson.JsonHelper;
import ru.azorium.launchserver.utils.MavenUrls;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ForgeInstaller {
    private static final Logger logger = LoggerFactory.getLogger(ForgeInstaller.class);
    private final String forgeMavenUrl = "https://maven.minecraftforge.net";
    private final String mcLibrariesUrl = "https://libraries.minecraft.net";
    private Path installerJarPath;
    private Path forgeManifestPath;
    private String fullVersion;

    private String mcVersion;
    private String loaderVersion;
    private Path clientPath;
    private Path librariesPath;
    private Path versionsPath;

    /**
     * Версии, путь до которых в <a href="https://maven.minecraftforge.net">Forge Maven</a> является формата {@code {mcVersion-forgeVersion-mcVersion}}
     **/
    private static List<String> clownFormat = new ArrayList<>();

    /**
     * Версии, установка Forge которых будет производиться через сам архив installer'a. Если версия не попадает в этот список,
     * установка происходит автоматическая через сам installer
     **/
    private static List<String> downloadFromInstaller = new ArrayList<>();

    /**
     * Библиотеки, которых нет в <a href="https://maven.minecraftforge.net">Forge Maven</a>. Ссылка заменяется на <a href="https://libraries.minecraft.net">Minecraft Libraries</a>
     **/
    private static List<String> badLibs = new ArrayList<>();

    static {
        downloadFromInstaller.add("1.12.1");
        downloadFromInstaller.add("1.12");
        downloadFromInstaller.add("1.11.2");
        downloadFromInstaller.add("1.11");
        downloadFromInstaller.add("1.10");
        downloadFromInstaller.add("1.9.4");
        downloadFromInstaller.add("1.9");
        downloadFromInstaller.add("1.8.9");
        downloadFromInstaller.add("1.8.8");
        downloadFromInstaller.add("1.8");
        downloadFromInstaller.add("1.7.10");

        clownFormat.add("1.10");
        clownFormat.add("1.9.4");
        clownFormat.add("1.9");
        clownFormat.add("1.8.9");
        clownFormat.add("1.8.8");
        clownFormat.add("1.8");
        clownFormat.add("1.7.10");

        badLibs.add("lzma");
        badLibs.add("launchwrapper");
    }

    public ForgeInstaller(String clientName, String mcVersion, String loaderVersion, Path versionsPath) {
        this.mcVersion = mcVersion;
        this.loaderVersion = loaderVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
        this.librariesPath = clientPath.resolve("libraries");
        this.versionsPath = versionsPath;
    }

    public void start() throws IOException {
        downloadInstaller(loaderVersion);

        copyForgeManifest();
        if (downloadFromInstaller.contains(mcVersion)) {
            downloadFromInstaller();
        } else {
            downloadWithInstaller();
        }
    }

    void downloadInstaller(String forgeLatestVersion) {
        logger.info("Установка инсталлера");
        fullVersion = clownFormat.contains(mcVersion)
                ? String.format("%s-%s-%s", mcVersion, forgeLatestVersion, mcVersion)
                : String.format("%s-%s", mcVersion, forgeLatestVersion);

        String fileName = String.format("forge-%s-installer.jar", fullVersion);
        String downloadUrl = String.format("%s/net/minecraftforge/forge/%s/%s", forgeMavenUrl, fullVersion, fileName);

        installerJarPath = clientPath.resolve(fileName);

        FileUtils.downloadFileFromURL(downloadUrl, installerJarPath);
    }

    void copyForgeManifest() throws IOException {
        String manifestFileName = downloadFromInstaller.contains(mcVersion) ? "install_profile.json" : "version.json";
        String version = mcVersion + "-" + loaderVersion;
        forgeManifestPath = versionsPath.resolve(version + "/" + version + ".json");
        FileUtils.createDirectories(forgeManifestPath.getParent());

        extractFromInstaller(manifestFileName, forgeManifestPath);
    }

    void downloadFromInstaller() throws IOException {
        JsonObject forgeManifestObject = JsonHelper.getObjectFromPath(forgeManifestPath);
        JsonArray librariesArray = JsonHelper.getArray(forgeManifestObject, "versionInfo", "libraries");

        logger.info("Установка библиотек: ");
        ProgressBarBuilder builder = new ProgressBarBuilder()
                .setStyle(GlobalObjects.pbStyle)
                .setInitialMax(librariesArray.asList().size() - 1)
                .setUnit(" Library", 1)
                .setUpdateIntervalMillis(100);

        ProgressBar.wrap(librariesArray.asList().parallelStream(), builder)
                .filter(el -> {
                    // Сам forge не качается, а берётся из архива installer'a
                    return !JsonHelper.getString(el.getAsJsonObject(), "name").contains("minecraftforge");
                })
                .forEach(el -> {
                    String libMaven = JsonHelper.getString(el.getAsJsonObject(), "name");
                    String pathedLibUrl = MavenUrls.mavenToPath(libMaven);

                    String urlForDownload = "";
                    for (String badLib : badLibs) {
                        if (libMaven.contains(badLib)) {
                            urlForDownload = mcLibrariesUrl;
                            break;
                        } else {
                            urlForDownload = forgeMavenUrl;
                        }
                    }

                    String libUrl = urlForDownload + "/" + pathedLibUrl;
                    Path libFullPath = librariesPath.resolve(pathedLibUrl);
                    if (!Files.exists(libFullPath)) {
                        FileUtils.createDirectories(librariesPath.resolve(pathedLibUrl).getParent());
                        FileUtils.downloadFileFromURL(libUrl, libFullPath);
                    }

                });
        
        try {
            String forgeFileName = String.format("forge-%s-universal.jar", fullVersion);
            Path forgeFilePath = librariesPath.resolve(String.format("net/minecraftforge/forge/%s/%s", fullVersion, forgeFileName));
            FileUtils.createDirectories(forgeFilePath.getParent());

            extractFromInstaller(forgeFileName, forgeFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void extractFromInstaller(String fileName, Path outFilePath) {
        try (JarFile jarFile = new JarFile(installerJarPath.toFile())) {
            JarEntry manifestEntry = jarFile.getJarEntry(fileName);

            try (InputStream is = jarFile.getInputStream(manifestEntry)) {
                FileOutputStream fos = new FileOutputStream(outFilePath.toFile());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void downloadWithInstaller() throws IOException {
        logger.info("Запуск инсталлера");
        Files.copy(forgeManifestPath, clientPath.resolve("launcher_profiles.json"), StandardCopyOption.REPLACE_EXISTING);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    installerJarPath.toString(),
                    "--installClient",
                    clientPath.toString()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[FORGE INSTALLER PROCESS] {}", line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Установка клиента {} завершилась: {}", clientPath.getFileName().toString(), exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}