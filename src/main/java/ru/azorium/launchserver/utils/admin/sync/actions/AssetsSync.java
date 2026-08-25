package ru.azorium.launchserver.utils.admin.sync.actions;

import ru.azorium.launchserver.utils.hash.Hashes;

public class AssetsSync {

    public static void start(String mcVersion) {
        Hashes.createAssetsManifest(mcVersion);
    }

}
