package com.orderprocessing.orderservice.service;

import com.orderprocessing.orderservice.entity.Order;
import com.orderprocessing.orderservice.OrderRepository;
import com.orderprocessing.orderservice.events.EventPublisher;
import com.orderprocessing.orderservice.events.OrderCreatedEvent;
import com.orderprocessing.orderservice.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private OrderStateMachine stateMachine;

    @Value("${payment.service.url:http://payment-service:8080}")
    private String paymentServiceUrl;

    @Value("${inventory.service.url:http://inventory-service:8080}")
    private String inventoryServiceUrl;

    @Value("${notification.service.url:http://notification-service:8080}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public Order processOrder(Order order)
    {
        // New orders always start in PENDING state
        order.setState(OrderState.PENDING);
        order = orderRepository.save(order);

        log.info("🛒 Order created: {} in state: {}", order.getId(), order.getState());
        // Publish OrderCreatedEvent
        String eventId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount().doubleValue(),
                LocalDateTime.now().toString()
        );
        eventPublisher.publishOrderCreated(event);

        // Transition to PROCESSING state
        transitionState(order, OrderState.PROCESSING);

        return order;
    }

    @Transactional
    public void transitionState(Order order, OrderState newState)
    {
        OrderState currentState = order.getState();

        // Validate transition using state machine
        OrderState validatedState = stateMachine.transition(currentState, newState);

        order.setState(validatedState);
        orderRepository.save(order);

        log.info("🔄 Order {} transitioned: {} → {}",
                order.getId(), currentState, validatedState);
    }

    @Transactional
    public void handlePaymentSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        transitionState(order, OrderState.CONFIRMED);
        log.info("✅ Order {} confirmed", orderId);
    }

    @Transactional
    public void handlePaymentFailure(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        transitionState(order, OrderState.FAILED);
        log.error("❌ Order {} failed", orderId);
    }
}