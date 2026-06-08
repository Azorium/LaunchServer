package ru.azorium.launchserver.commands.logic.syncUp.actions;

import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.hash.Hashes;
import ru.azorium.launchserver.utils.hash.Md5;

public class LauncherSync {

    public static void start(){
        LauncherApplication.runtimeZipHash = Md5.getFileMd5(LauncherApplication.runtimeZipPath);
        Hashes.createLauncherManifest();
    }

}
