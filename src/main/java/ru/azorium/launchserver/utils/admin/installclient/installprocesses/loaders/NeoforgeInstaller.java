package ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders;

import ru.azorium.launchserver.utils.admin.EventLogger;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;
import ru.azorium.launchserver.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class NeoforgeInstaller {
    static final String neoforgeMavenUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    static Path installerPath;

    private final String mcVersion;
    private final String loaderVersion;
    private final Path clientPath;
    private final String clientName;
    private final InstallContext context;

    public NeoforgeInstaller(InstallContext context) {
        this.mcVersion = context.mcVersion();
        this.loaderVersion = context.loaderVersion();
        this.clientPath = context.clientPath();
        this.clientName = context.clientName();
        this.context = context;
    }

    public void start() throws IOException {
        String logMessage = "Установка инсталлера %s".formatted(context.loaderType());
        context.logger().info(new EventLogger.InstallEventMessage(null, logMessage, logMessage));

        installerPath = clientPath.resolve("neoforge-installer.jar");
        String installerDownloadUrl = String.format("%s%s/neoforge-%s-installer.jar", neoforgeMavenUrl, loaderVersion, loaderVersion);

        FileUtils.downloadFileFromURL(new URL(installerDownloadUrl).toString(), installerPath);
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
                    "--installClient",
                    clientPath.toString()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    context.logger().info(new EventLogger.InstallEventMessage(null, null, "[NEOFORGE INSTALLER PROCESS]: " + line));
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
