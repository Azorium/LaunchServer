package ru.azorium.launchserver.commands.logic.syncUp.actions;

import ru.azorium.launchserver.utils.hash.Hashes;

public class ClientsSync {

    public static void start(String folderName) {
        Hashes.createClientsManifest(folderName);
    }
    
}
