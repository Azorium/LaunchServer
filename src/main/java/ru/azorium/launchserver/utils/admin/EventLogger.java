package ru.azorium.launchserver.utils.admin;

public interface EventLogger {
    record InstallEventMessage(Integer progress, String stage, String log) {
    }

    void info(Object message);

    void error(Throwable ex);

    void complete();
}

