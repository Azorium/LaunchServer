package ru.azorium.launchserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.azorium.launchserver.utils.admin.EventLogger;

@Service
public class SseLoggerService {

    private final SseService sseService;

    public SseLoggerService(SseService sseService) {
        this.sseService = sseService;
    }

    public EventLogger create(int emitterKey) {
        return new EventLogger() {
            @Override
            public void info(Object message) {
                sseService.send(emitterKey, message);
            }

            @Override
            public void error(Throwable ex) {
                sseService.completeWithError(emitterKey, ex);
            }

            @Override
            public void complete() {
                sseService.send(emitterKey, SseEmitter.event().name("complete").data("Установка завершена"));
                sseService.complete(emitterKey);
            }
        };
    }
}
