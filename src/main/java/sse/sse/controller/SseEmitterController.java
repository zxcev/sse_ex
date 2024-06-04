package sse.sse.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseEmitterController {

    @GetMapping("/sse")
    public SseEmitter sseEmitter() {
        SseEmitter sseEmitter = new SseEmitter();

        return sseEmitter;
    }
}
