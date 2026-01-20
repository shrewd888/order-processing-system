package com.orderprocessing.orderservice.controller;

import com.orderprocessing.orderservice.events.EventPublisher;
import com.orderprocessing.orderservice.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EventPublisher eventPublisher;

    @PostMapping("/duplicate")
    public String testDuplicate(@RequestParam Long orderId) {
        // Generate a FIXED event ID (not random)
        String eventId = "TEST-DUPLICATE-" + orderId;

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,  // Same eventId for duplicates
                orderId,
                "Duplicate Test User",
                100.0,
                LocalDateTime.now().toString()
        );

        // Publish TWICE with same eventId
        eventPublisher.publishOrderCreated(event);
        eventPublisher.publishOrderCreated(event);  // DUPLICATE!

        return "Published event " + eventId + " TWICE. Check logs for duplicate detection!";
    }
}