package ru.azorium.launchserver.utils.admin.sync;

import ru.azorium.launchserver.utils.admin.sync.actions.AssetsSync;
import ru.azorium.launchserver.utils.admin.sync.actions.ClientsSync;
import ru.azorium.launchserver.utils.admin.sync.actions.LauncherSync;
import ru.azorium.launchserver.utils.admin.sync.actions.ProfilesSync;

public class Sync {

    public static void start(SyncContext context) {
        String value = context.value();

        switch (context.action()) {
            case "profiles" -> ProfilesSync.start();
            case "client" -> ClientsSync.start(value);
            case "asset" -> AssetsSync.start(value);
            case "launcher" -> LauncherSync.start();
            case "all" -> {
                ProfilesSync.start();
                ClientsSync.start(value);
                AssetsSync.start(value);
                LauncherSync.start();
            }
        }

        if (context.eventLogger() != null) {
            context.eventLogger().complete();
        }
    }

}
