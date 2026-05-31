package ru.azorium.launchserver.commands.logic.syncUp.actions;

import ru.azorium.launchserver.utils.hash.Hashes;

public class AssetsSync {

    public static void start(String mcVersion) {
        Hashes.createAssetsManifest(mcVersion);
    }

}
