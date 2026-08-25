package ru.azorium.launchserver.controllers;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.azorium.launchserver.service.SseService;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter create(){
        SseService.EmitterInfo emitterInfo = sseService.createEmitter(600_000L);
        sseService.send(emitterInfo.emitterKey(), emitterInfo.emitterKey());
        return emitterInfo.emitter();
    }

}
