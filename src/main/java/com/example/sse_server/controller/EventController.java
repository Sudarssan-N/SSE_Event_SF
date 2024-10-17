package com.example.sse_server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@RestController
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public EventController() {
        logger.info("Initializing EventController...");
        scheduler.scheduleAtFixedRate(() -> {
            String randomMessage = "Random Event: " + random.nextInt(100);
            logger.debug("Generated random message: {}", randomMessage);
            broadcastEvent(randomMessage);

            // Send a heartbeat every 30 seconds to keep the connection alive
            broadcastEvent(": keep-alive\n");
        }, 0, 30, TimeUnit.SECONDS);
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        logger.info("New subscription request received.");
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour
        emitters.add(emitter);
        logger.debug("Added new SseEmitter. Total emitters: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.debug("Emitter completed. Total emitters: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.debug("Emitter timed out. Total emitters: {}", emitters.size());
        });

        emitter.onError((e) -> {
            emitters.remove(emitter);
            logger.debug("Emitter error. Total emitters: {}", emitters.size());
        });

        return emitter;
    }

    private void broadcastEvent(String eventData) {
        logger.info("Broadcasting event: {}", eventData);
        emitters.forEach(emitter -> {
            try {
                emitter.send(eventData);
                logger.debug("Successfully sent event to emitter.");
            } catch (Exception e) {
                emitters.remove(emitter);
                logger.error("Failed to send event to emitter. Removing emitter. Total emitters: {}", emitters.size(), e);
            }
        });
    }
}
