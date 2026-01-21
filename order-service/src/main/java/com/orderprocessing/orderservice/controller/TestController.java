package com.orderprocessing.orderservice.controller;

import com.orderprocessing.orderservice.OrderRepository;
import com.orderprocessing.orderservice.entity.Order;
import com.orderprocessing.orderservice.events.EventPublisher;
import com.orderprocessing.orderservice.events.OrderCreatedEvent;
import com.orderprocessing.orderservice.model.OrderState;
import com.orderprocessing.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;

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


    @PostMapping("/invalid-transition/{orderId}")
    public String testInvalidTransition(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            // Try invalid transition: CONFIRMED → PENDING
            orderService.transitionState(order, OrderState.PENDING);
            return "ERROR: Transition should have been blocked!";
        } catch (IllegalStateException e) {
            return "✅ Transition correctly blocked: " + e.getMessage();
        }
    }
}