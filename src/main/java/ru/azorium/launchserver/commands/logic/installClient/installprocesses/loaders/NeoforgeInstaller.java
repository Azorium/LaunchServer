package ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.commands.logic.installClient.installprocesses.MinecraftInstall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NeoforgeInstaller {
    private static final Logger logger = LoggerFactory.getLogger(NeoforgeInstaller.class);
    static String neoforgeMavenUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    static Path installerPath;

    private String mcVersion;
    private String loaderVersion;
    private Path clientPath;

    public NeoforgeInstaller(String clientName, String mcVersion, String loaderVersion) {
        this.mcVersion = mcVersion;
        this.loaderVersion = loaderVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
    }

    public void start() {
        logger.info("Установка инсталлера");
        installerPath = clientPath.resolve("neoforge-installer.jar");
        String installerDownloadUrl = String.format("%s%s/neoforge-%s-installer.jar", neoforgeMavenUrl, loaderVersion, loaderVersion);

        try (InputStream is = new URL(installerDownloadUrl).openStream()) {
            Files.copy(is, installerPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(clientPath.resolve("versions").resolve(mcVersion).resolve(mcVersion + ".json"), clientPath.resolve("launcher_profiles.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        installerExec();
    }

    void installerExec() {
        try {
            logger.info("Запуск инсталлера");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    installerPath.toString(),
                    "--installClient",
                    clientPath.toString()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[NEOFORGE INSTALLER PROCESS] {}", line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Установка клиента {} завершилась: {}", clientPath.getFileName().toString(), exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
