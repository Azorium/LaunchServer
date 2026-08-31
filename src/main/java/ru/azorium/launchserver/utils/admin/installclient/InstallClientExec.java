package ru.azorium.launchserver.utils.admin.installclient;

import org.springframework.stereotype.Component;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.CreateProfileJson;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.MinecraftInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.FabricInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.ForgeInstaller;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.loaders.NeoforgeInstaller;
import ru.azorium.launchserver.utils.FileUtils;
import ru.azorium.launchserver.utils.admin.installclient.installprocesses.NativeExtractor;

import java.io.IOException;

public class InstallClientExec {

    public void start(InstallContext context, AppPaths appPaths) throws IOException {
        FileUtils.createDirectories(context.clientPath());
        new MinecraftInstaller(context, appPaths).start();
        NativeExtractor.start(context);
        loaderInstall(context);
        new CreateProfileJson(context, appPaths).start();
        context.logger().complete();
    }

    private void loaderInstall(InstallContext context) throws IOException {
        switch (context.loaderType()) {
            case "forge" -> new ForgeInstaller(context).start();
            case "fabric" -> new FabricInstaller(context).start();
            case "neoforge" -> new NeoforgeInstaller(context).start();
            case "vanilla" -> {
            }
        }
    }

}
