package ru.azorium.launchserver.commands.logic.syncUp.actions;

import ru.azorium.launchserver.utils.hash.Hashes;

public class LauncherSync {

    public static void start(){
        Hashes.createLauncherManifest();
    }

}
