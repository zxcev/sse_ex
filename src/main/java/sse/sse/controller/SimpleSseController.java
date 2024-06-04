package sse.sse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import sse.sse.model.Count;
import sse.sse.pool.CustomThreadPool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SimpleSseController {

    private final ObjectMapper op;
    private final Map<Integer, PrintWriter> writeStreams = new ConcurrentHashMap<>();
    private final CustomThreadPool thread;


    @GetMapping(value = "/ping")
    public String ping() {
        return "ping";
    }

    @GetMapping(value = "/sse/{userId}")
    public String sse(@PathVariable("userId") int userId, HttpServletResponse response) throws InterruptedException, IOException {
        log.info("userId: {} connected to sseSync", userId);
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        writeStreams.put(userId, w);

        return "end";
    }

    @GetMapping(value = "/sse/blocking/{userId}")
    public String sseBlocking(@PathVariable("userId") int userId, HttpServletResponse response) throws InterruptedException, IOException {
        log.info("userId: {} connected to sseSync", userId);
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        writeStreams.put(userId, w);

        while (true) {
            Thread.sleep(1_000);
        }
    }


    @GetMapping(value = "/sse/async/{userId}")
    public void sseAsync(HttpServletRequest request, @PathVariable("userId") int userId) throws InterruptedException, IOException {
        log.info("userId: {} connected to sseAsync", userId);


        AsyncContext ctx = request.startAsync();
        ctx.setTimeout(0);
        HttpServletResponse res = (HttpServletResponse) ctx.getResponse();
        res.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        res.setCharacterEncoding("UTF-8");
        PrintWriter writer = res.getWriter();
        writeStreams.put(userId, writer);


        ctx.start(() -> {
            log.info("ctx start for userId={}", userId);
        });

        ctx.addListener(new AsyncListener() {
            @Override
            public void onStartAsync(AsyncEvent asyncEvent) {
                log.info("Async start for userId={}", userId);
            }

            @Override
            public void onComplete(AsyncEvent asyncEvent) {
                writeStreams.remove(userId);
                log.info("userId={} sse connection safely released", userId);
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) {
                writeStreams.remove(userId);
                log.warn("userId={} sse connection timed out ", userId);
            }

            @Override
            public void onError(AsyncEvent asyncEvent) {
                writeStreams.remove(userId);
                log.error("userId={} sse connection unexpectedly disconnected", userId);
            }
        });


    }
//
//    @GetMapping(value = "/sse/async/{userId}")
//    public void sseAsync(HttpServletRequest request, HttpServletResponse res, @PathVariable("userId") int userId) throws InterruptedException, IOException {
//        log.info("userId: {} connected to sseAsync", userId);
//
//        res.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
//        res.setCharacterEncoding("UTF-8");
//        PrintWriter writer = res.getWriter();
//        writeStreams.put(userId, writer);
//
//        AsyncContext ctx = request.startAsync();
//        ctx.setTimeout(0);
//
//        ctx.start(() -> {
//            log.info("ctx start for userId={}", userId);
//        });
//
//        ctx.addListener(new AsyncListener() {
//            @Override
//            public void onStartAsync(AsyncEvent asyncEvent) {
//                log.info("Async start for userId={}", userId);
//            }
//
//            @Override
//            public void onComplete(AsyncEvent asyncEvent) {
//                writeStreams.remove(userId);
//                log.info("userId={} sse connection safely released", userId);
//            }
//
//            @Override
//            public void onTimeout(AsyncEvent asyncEvent) {
//                writeStreams.remove(userId);
//                log.warn("userId={} sse connection timed out ", userId);
//            }
//
//            @Override
//            public void onError(AsyncEvent asyncEvent) {
//                writeStreams.remove(userId);
//                log.error("userId={} sse connection unexpectedly disconnected", userId);
//            }
//        });
//
//
//    }

    @PostMapping("/emit/count/{userId}")
    public void emitCount(@PathVariable("userId") int userId, @RequestBody Count count) {
        log.info("userId: {} emitted to count", userId);
        thread.run(() -> {
            try {
                int retryCount = 3;
                PrintWriter w = writeStreams.get(userId);
                while (retryCount-- > 0) {
                    w = writeStreams.get(userId);
                    if (w != null) {
                        Thread.sleep(2_000);
                        String countJson = op.writeValueAsString(count);
                        w.write("id:" + userId + "\n");
                        w.write("event: cnt" + "\n");
                        w.write("data:" + countJson + "\n\n");
                        w.flush();
//                w0.write("data: " + count + "\n\n");
                        log.info("safely emitCount done");
                        return;
                    }
                    Thread.sleep(2_000);
                    log.info("got null w, emitCount retry... retryCount = {}", retryCount);
                }

                if (w != null) {

                    w.close();
                }
//                PrintWriter w0 = writeStreams.get(0);
//                w.write("type: Counter\n");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @PostMapping("/emit/count/unsafe/{userId}")
    public void emitSafeCount(@PathVariable("userId") int userId, @RequestBody Count count) {
        log.info("userId: {} emitted to count", userId);
        thread.run(() -> {
            try {
                Thread.sleep(2_000);
                PrintWriter w = writeStreams.get(userId);
//                PrintWriter w0 = writeStreams.get(0);
//                w.write("type: Counter\n");
                String countJson = op.writeValueAsString(count);
                w.write("id:" + userId + "\n");
                w.write("event: cnt" + "\n");
                w.write("data:" + countJson + "\n\n");
                w.flush();
//                w0.write("data: " + count + "\n\n");
                log.info("done");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
