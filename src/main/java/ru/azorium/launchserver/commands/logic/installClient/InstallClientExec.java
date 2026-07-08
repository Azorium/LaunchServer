package ru.azorium.launchserver.commands.logic.installClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.commands.logic.syncUp.SyncUp;
import ru.azorium.launchserver.commands.logic.syncUp.actions.ProfilesSync;
import ru.azorium.launchserver.utils.CreateProfileJson;
import ru.azorium.launchserver.commands.logic.installClient.installprocesses.MinecraftInstaller;
import ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders.FabricInstaller;
import ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders.ForgeInstaller;
import ru.azorium.launchserver.commands.logic.installClient.installprocesses.loaders.NeoforgeInstaller;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.NativeExtractor;

import java.io.IOException;
import java.nio.file.Path;

public class InstallClientExec {
    private static final Logger logger = LoggerFactory.getLogger(InstallClientExec.class);
    public String clientName;
    public String mcVersion;
    public String loaderType;
    public String loaderVersion;

    public Path clientPath;
    public Path librariesPath;
    public Path nativesPath;
    public Path versionsPath;

    public InstallClientExec(String clientName, String mcVersion, String loaderType, String loaderVersion) throws IOException {
        this.clientName = clientName;
        this.mcVersion = mcVersion;
        this.loaderType = loaderType;
        this.loaderVersion = loaderVersion;
        this.clientPath = LauncherApplication.clientsPath.resolve(clientName);
        this.librariesPath = clientPath.resolve("libraries");
        this.nativesPath = clientPath.resolve("natives");
        this.versionsPath = clientPath.resolve("versions");

        FileUtils.createDirectories(clientPath);
    }

    public void start() throws IOException {
        boolean isInstalledAssets = new MinecraftInstaller(clientName, mcVersion).start();
        NativeExtractor.start(versionsPath.resolve(mcVersion).resolve(mcVersion + ".json"), librariesPath, nativesPath);

        switch (loaderType) {
            case "forge" -> new ForgeInstaller(clientName, mcVersion, loaderVersion, versionsPath).start();
            case "fabric" -> new FabricInstaller(clientName, mcVersion, loaderVersion).start();
            case "neoforge" -> new NeoforgeInstaller(clientName, mcVersion, loaderVersion).start();
            case "vanilla" -> {
                return;
            }
            default -> {
                logger.info("Неверный тип загрузчика: {}", loaderType);
                return;
            }
        }

        new CreateProfileJson(clientName, mcVersion, loaderType, loaderVersion).start();

        ProfilesSync.start();
        SyncUp.start("client", clientName);

        if (!isInstalledAssets) {
            SyncUp.start("asset", mcVersion);
        }
    }

}
