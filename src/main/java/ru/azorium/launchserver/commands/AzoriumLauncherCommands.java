package ru.azorium.launchserver.commands;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import ru.azorium.launchserver.commands.logic.installClient.InstallClientExec;
import ru.azorium.launchserver.commands.logic.syncUp.SyncUp;

import java.io.IOException;

@Component
public class AzoriumLauncherCommands {

    @Command(name = "installclient", description = "Установка клиента")
    public void installClient(
            @Option(shortName = 'n', longName = "name", description = "Название клиента", required = true) String clientName,
            @Option(shortName = 't', longName = "type", description = "Загрузчик модов", required = true) String loaderType,
            @Option(shortName = 'm', longName = "mcversion", description = "Версия Minecraft", required = true) String mcVersion,
            @Option(shortName = 'l', longName = "loaderversion", description = "Версия загрузчика", required = true) String loaderVersion
    ) throws IOException {
        new InstallClientExec(clientName, mcVersion, loaderType, loaderVersion).start();
    }

    @Command(name = "syncup", description = "Применение незарегистрированных изменений")
    public void syncUp(
            @Option(shortName = 'a', longName = "action", description = "Действие (profiles, client <folderName>, asset <version>)", required = true) String actionType,
            @Option(shortName = 'v', longName = "value", description = "Значение", defaultValue = "none") String value
    ) {
        SyncUp.start(actionType, value);
    }
}