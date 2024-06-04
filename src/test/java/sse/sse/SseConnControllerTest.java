package sse.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SseConnControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testSseStream() throws Exception {
        BlockingQueue<String> results = new LinkedBlockingQueue<>();

        // Start the SSE stream in a separate thread
        new Thread(() -> {
            try {
                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/sse/stream"))
                        .andExpect(status().isOk())
                        .andReturn();

                result.getResponse().getContentAsByteArray();
                results.put(new String(result.getResponse().getContentAsByteArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Trigger an event
        Thread.sleep(1000); // Ensure the SSE stream is set up
        mockMvc.perform(MockMvcRequestBuilders.post("/sse/trigger"))
                .andExpect(status().isOk());

        // Check if the event was received
        String response = results.take();
        assertThat(response).contains("Test Event");
    }

}