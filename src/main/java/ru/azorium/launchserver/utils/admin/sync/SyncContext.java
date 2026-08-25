package ru.azorium.launchserver.utils.admin.sync;

import ru.azorium.launchserver.utils.admin.EventLogger;

public record SyncContext (String action, String value, EventLogger eventLogger){
}
