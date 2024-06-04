package sse.sse.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/noend")
    public String noend() throws InterruptedException {
        Thread.sleep(10000000L);
        return "end";
    }

    @GetMapping("/end")
    public String end() throws InterruptedException {
        return "end";
    }
}
