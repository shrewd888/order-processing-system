package com.orderprocessing.orderservice.events;

import com.orderprocessing.orderservice.Order;
import com.orderprocessing.orderservice.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventListener {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(
            topics = "payment-success",
            groupId = "order-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.orderservice.events.PaymentSuccessEvent"
            }
    )
    public void handlePaymentSuccess(@Payload PaymentSuccessEvent event) {
        log.info("📥 Received PaymentSuccessEvent: {}", event);

        // Update order status to CONFIRMED
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            log.info("✅ Order #{} status updated to CONFIRMED", event.getOrderId());
        });
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.orderservice.events.PaymentFailedEvent"
            }
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("📥 Received PaymentFailedEvent: {}", event);

        // Update order status to FAILED
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("FAILED - " + event.getReason());
            orderRepository.save(order);
            log.error("❌ Order #{} status updated to FAILED", event.getOrderId());
        });
    }
}