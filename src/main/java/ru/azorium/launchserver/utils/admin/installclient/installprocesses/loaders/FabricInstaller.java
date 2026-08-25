package ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders;

import ru.azorium.launchserver.utils.admin.EventLogger;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;
import ru.azorium.launchserver.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricInstaller {
    private static final String installerDownloadUrl = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.1/fabric-installer-1.1.1.jar";

    private final Path installerPath;
    private final String mcVersion;
    private final String loaderVersion;
    private final Path clientPath;
    private final InstallContext context;

    public FabricInstaller(InstallContext context) {
        this.mcVersion = context.mcVersion();
        this.loaderVersion = context.loaderVersion();
        this.clientPath = context.clientPath();
        this.installerPath = clientPath.resolve("fabric-installer.jar");
        this.context = context;
    }

    public void start() throws IOException {
        String logMessage = "Установка инсталлера %s".formatted(context.loaderType());
        context.logger().info(new EventLogger.InstallEventMessage(null, logMessage, logMessage));

        FileUtils.downloadFileFromURL(installerDownloadUrl, installerPath);
        Files.copy(clientPath.resolve("versions").resolve(mcVersion).resolve(mcVersion + ".json"), clientPath.resolve("launcher_profiles.json"));

        installerExec();
    }

    private void installerExec() {
        try {
            String logMessage = "Установка %s".formatted(context.loaderType());
            context.logger().info(new EventLogger.InstallEventMessage(null, logMessage, logMessage));

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
                    context.logger().info(new EventLogger.InstallEventMessage(null, null, "[FABRIC INSTALLER PROCESS]: " + line));
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
