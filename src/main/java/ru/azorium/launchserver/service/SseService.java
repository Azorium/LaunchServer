package ru.azorium.launchserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.azorium.launchserver.exception.ClientFolderAlreadyExistException;
import ru.azorium.launchserver.utils.admin.EventLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public record EmitterInfo(SseEmitter emitter, Integer emitterKey) {
    }

    public EmitterInfo createEmitter(Long time) {
        int emitterKey = emitters.size();
        SseEmitter emitter = new SseEmitter(time);

        emitter.onCompletion(() -> emitters.remove(emitterKey));
        emitter.onError((t) -> emitters.remove(emitterKey));
        emitter.onTimeout(() -> emitters.remove(emitterKey));

        emitters.put(emitterKey, emitter);

        return new EmitterInfo(emitter, emitterKey);
    }

    public SseEmitter getEmitter(int emitterKey) {
        return emitters.get(emitterKey);
    }

    public void send(int emitterKey, Object message) {
        SseEmitter emitter = getEmitter(emitterKey);

        if (emitter == null) return;

        try {
            emitter.send(message);
        } catch (IOException e) {
            emitters.remove(emitterKey);
            emitter.completeWithError(e);
        }
    }

    public void send(int emitterKey, SseEmitter.SseEventBuilder builder) {
        SseEmitter emitter = getEmitter(emitterKey);

        if (emitter == null) return;

        try {
            emitter.send(builder);
        } catch (IOException e) {
            emitters.remove(emitterKey);
            emitter.completeWithError(e);
        }
    }

    public void complete(int emitterKey) {
        SseEmitter emitter = getEmitter(emitterKey);
        emitter.complete();
    }

    public void completeWithError(int emitterKey, Throwable ex) {
        SseEmitter emitter = getEmitter(emitterKey);
        emitter.completeWithError(ex);

    }
}
