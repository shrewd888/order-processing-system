package com.orderprocessing.orderservice.controller;

import com.orderprocessing.orderservice.entity.Order;
import com.orderprocessing.orderservice.OrderRepository;
import com.orderprocessing.orderservice.service.OrderService;
import com.orderprocessing.orderservice.events.EventPublisher;
import com.orderprocessing.orderservice.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController
{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EventPublisher eventPublisher;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.processOrder(order);
    }

    // GET ALL orders (NEW - can test in browser!)
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @PostMapping("/test/duplicate/{orderId}")
    public String testDuplicate(@PathVariable Long orderId) {
        String eventId = UUID.randomUUID().toString();
        String correlationId = "corr-" + UUID.randomUUID().toString();

        OrderCreatedEvent event = new OrderCreatedEvent(
                correlationId,
                eventId,
                orderId,
                "Duplicate Test",
                100.0,
                LocalDateTime.now().toString()
        );

        // Publish TWICE with same eventId
        eventPublisher.publishOrderCreated(event);
        eventPublisher.publishOrderCreated(event);  // DUPLICATE!

        return "Published duplicate event: " + eventId;
    }
}