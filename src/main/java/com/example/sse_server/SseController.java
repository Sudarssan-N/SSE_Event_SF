package com.example.sse_server;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/sse")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);
    private final Random random = new Random();

    @CrossOrigin(origins = "https://wise-hawk-hdi2dp-dev-ed.trailblaze.my.salesforce.com")
    @GetMapping(value = "/sse/random-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamRandomEvents() {
        logger.info("New SSE connection established");
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {
                    String message = generateRandomMessage(sequence);
                    logger.info("Sending message: {}", message);
                    return "data: " + message + "\n\n";
                })
                .doOnCancel(() -> logger.info("SSE connection closed by client"));
    }

    private String generateRandomMessage(Long sequence) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", sequence);
        data.put("timestamp", System.currentTimeMillis());
        data.put("value", random.nextDouble());
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("Error processing JSON", e);
            return "{}";  // Return an empty JSON object or handle it as needed
        }
    }

}

