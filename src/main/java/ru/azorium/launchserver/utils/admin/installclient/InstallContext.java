package ru.azorium.launchserver.utils.admin.installclient;

import ru.azorium.launchserver.utils.admin.EventLogger;

import java.nio.file.Path;

public record InstallContext(String clientName, String mcVersion, String loaderType, String loaderVersion,
                             Path clientPath, Path librariesPath, Path nativesPath, Path versionsPath,
                             EventLogger logger) {

}

