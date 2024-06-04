package sse.sse.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sse.sse.repo.EmittersRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sse-connect")
public class SseConnController {

    private static final Logger log = LoggerFactory.getLogger(SseConnController.class);
    @Autowired
    private EmittersRepository emittersRepository;

    private static final Long DEFAULT_TIMEOUT = 1000 * 60L; // SSE 유효시간
    private static final List<Integer> counts = new ArrayList<>();

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String nextEventCount(
            @PathVariable("userId") Integer userId
    ) throws IOException {
        connect(userId);
        return String.format("connected to userId: %d\n", userId);
    }

    @GetMapping("/count")
    public String count(@PathVariable("userId") Integer userId) {
        sendCount(userId);
        return "ok";
    }

    public SseEmitter connect(int userId) throws IOException {

        SseEmitter emitter = new SseEmitter((DEFAULT_TIMEOUT));

        emitter = emittersRepository.registerEmitter(userId, emitter);

        emitter.send(SseEmitter.event()
                .id(String.format("%d", userId))
                .name("connection")
                .data("SSE 연결이 되었습니다."));
        return emitter;
    }

    public void sendCount(Integer userId) {
        int count = emittersRepository.getAndAdd(userId);
        SseEmitter emitter = emittersRepository.getEmitter(userId);
        try {
            emitter.send(SseEmitter.event()
                    .id(userId.toString())
                    .name("count")
                    .data(count)
            );
        } catch (Exception e) {
            log.error("오류 발생", e);
        }
    }

}
