package ru.azorium.launchserver.shell;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.azorium.launchserver.utils.admin.sync.Sync;
import ru.azorium.launchserver.utils.admin.sync.SyncContext;

@Component
public class ShellCommands {

    @Command(name = "syncup", description = "Применение незарегистрированных изменений")
    public void syncUp(
            @Option(shortName = 'a', longName = "action", description = "Действие (profiles, client <folderName>, asset <version>)", required = true) String action,
            @Option(shortName = 'v', longName = "value", description = "Значение", defaultValue = "none") String value
    ) {
        SyncContext context = new SyncContext(action, value, null);
        Sync.start(context);
    }

}
