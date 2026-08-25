package ru.azorium.launchserver.utils.admin.installclient;

import ru.azorium.launchserver.utils.admin.sync.Sync;
import ru.azorium.launchserver.utils.admin.sync.actions.ProfilesSync;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.CreateProfileJson;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.MinecraftInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.FabricInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.ForgeInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.NeoforgeInstaller;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.NativeExtractor;

import java.io.IOException;
import java.nio.file.Path;

public class InstallClientExec {
    private final InstallContext context;

    public InstallClientExec(InstallContext context) {
        this.context = context;
    }

    public void start() throws IOException {
        FileUtils.createDirectories(context.clientPath());
        boolean isInstalledAssets = new MinecraftInstaller(context).start();

        NativeExtractor.start(context);
        startLoaderInstall();
        new CreateProfileJson(context).start();

        ProfilesSync.start();

        context.logger().complete();
    }

    private void startLoaderInstall() throws IOException {
        switch (context.loaderType()) {
            case "forge" -> new ForgeInstaller(context).start();
            case "fabric" -> new FabricInstaller(context).start();
            case "neoforge" -> new NeoforgeInstaller(context).start();
            case "vanilla" -> {
            }
        }
    }

}
