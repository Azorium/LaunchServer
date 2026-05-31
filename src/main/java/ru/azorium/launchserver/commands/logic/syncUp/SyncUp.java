package ru.azorium.launchserver.commands.logic.syncUp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.commands.logic.syncUp.actions.AssetsSync;
import ru.azorium.launchserver.commands.logic.syncUp.actions.ClientsSync;
import ru.azorium.launchserver.commands.logic.syncUp.actions.LauncherSync;
import ru.azorium.launchserver.commands.logic.syncUp.actions.ProfilesSync;
import ru.azorium.launchserver.controllers.FileDownloadController;

public class SyncUp {

    private static final Logger logger = LoggerFactory.getLogger(SyncUp.class);

    public static void start(String action, String value) {
        switch (action) {
            case "profiles" -> {
                ProfilesSync.start();
                logger.info("Профили были обновлены");
            }
            case "client" -> {
                ClientsSync.start(value);
                logger.info("Клиент {} был обновлён", value);
            }
            case "asset" -> {
                AssetsSync.start(value);
                logger.info("Ассет {} был обновлён", value);
            }
            case "launcher" -> {
                LauncherSync.start();
                logger.info("Лаунчер был обновлён");
            }
            case "all" -> {
                ProfilesSync.start();
                ClientsSync.start("all");
                AssetsSync.start("all");
                LauncherSync.start();
                logger.info("Все компоненты лаунчера были обновлены");
            }
        }
    }

}
