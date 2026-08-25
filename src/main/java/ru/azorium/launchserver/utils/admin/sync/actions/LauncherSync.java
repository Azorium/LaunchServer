package ru.azorium.launchserver.utils.admin.sync.actions;

import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.hash.Hashes;
import ru.azorium.launchserver.utils.hash.Md5;

public class LauncherSync {

    public static void start(){
        LauncherApplication.runtimeZipHash = Md5.getFileMd5(LauncherApplication.runtimeZipPath);
        Hashes.createLauncherManifest();
    }

}
