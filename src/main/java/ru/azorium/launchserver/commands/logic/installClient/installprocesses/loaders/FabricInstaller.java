package ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FabricInstaller {
    private static final Logger logger = LoggerFactory.getLogger(FabricInstaller.class);

    static String installerDownloadUrl = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.1/fabric-installer-1.1.1.jar";
    static Path installerPath;

    public String mcVersion;
    public String loaderVersion;
    public Path clientPath;

    public FabricInstaller(String clientName, String mcVersion, String loaderVersion) {
        this.mcVersion = mcVersion;
        this.loaderVersion = loaderVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
    }

    public void start() throws IOException {
        logger.info("Установка инсталлера");
        installerPath = clientPath.resolve("fabric-installer.jar");

        FileUtils.downloadFileFromURL(installerDownloadUrl, installerPath);
        Files.copy(clientPath.resolve("versions").resolve(mcVersion).resolve(mcVersion + ".json"), clientPath.resolve("launcher_profiles.json"));

        installerExec();
    }

    void installerExec() {
        try {
            logger.info("Запуск инсталлера");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    installerPath.toString(),
                    "client",
                    "-dir",
                    clientPath.toString(),
                    "-mcversion",
                    mcVersion,
                    "-loader",
                    loaderVersion
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[FABRIC INSTALLER PROCESS] {}", line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Установка клиента {} завершилась: {}", clientPath.getFileName().toString(), exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
