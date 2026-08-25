package ru.azorium.launchserver.utils.admin.sync.actions;

import ru.azorium.launchserver.utils.hash.Hashes;

public class ClientsSync {

    public static void start(String folderName) {
        Hashes.createClientsManifest(folderName);
    }
    
}
