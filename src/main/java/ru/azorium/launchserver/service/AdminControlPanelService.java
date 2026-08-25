package ru.azorium.launchserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.exception.ClientFolderAlreadyExistException;
import ru.azorium.launchserver.utils.admin.EventLogger;
import ru.azorium.launchserver.utils.admin.installclient.InstallClientExec;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;
import ru.azorium.launchserver.model.dto.AdminRequests;
import ru.azorium.launchserver.utils.admin.sync.Sync;
import ru.azorium.launchserver.utils.admin.sync.SyncContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AdminControlPanelService {
    private final SseLoggerService sseLoggerService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AdminControlPanelService(SseLoggerService sseLoggerService) {
        this.sseLoggerService = sseLoggerService;
    }

    public void installClient(AdminRequests.InstallClientRequest installClientRequest) {
        Path clientPath = LauncherApplication.clientsPath.resolve(installClientRequest.getFolderName());
        EventLogger eventLogger = sseLoggerService.create(installClientRequest.getEmitterKey());

        if (Files.exists(clientPath)) {
            ClientFolderAlreadyExistException ex = new ClientFolderAlreadyExistException("Данный клиент уже существует.");
            eventLogger.error(ex);
            throw ex;
        }

        InstallContext context = new InstallContext(
                installClientRequest.getFolderName(),
                installClientRequest.getGameVersion(),
                installClientRequest.getLoaderType(),
                installClientRequest.getLoaderVersion(),
                clientPath,
                clientPath.resolve("libraries"),
                clientPath.resolve("natives"),
                clientPath.resolve("versions"),
                eventLogger
        );

        executor.submit(() -> {
            try {
                new InstallClientExec(context).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sync(@RequestBody AdminRequests.SyncRequest syncRequest){
        EventLogger eventLogger = sseLoggerService.create(syncRequest.getEmitterKey());

        SyncContext context = new SyncContext(
                syncRequest.getAction(),
                syncRequest.getValue(),
                eventLogger
        );

        executor.submit(() -> {
            Sync.start(context);
        });
    }

}
