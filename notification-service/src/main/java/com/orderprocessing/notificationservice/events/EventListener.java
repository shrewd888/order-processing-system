package com.orderprocessing.notificationservice.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Component
@Slf4j
public class EventListener {

    @KafkaListener(
            topics = "payment-success",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.notificationservice.events.PaymentSuccessEvent"
            }
    )
    public void handlePaymentSuccess(@Payload PaymentSuccessEvent event) {
        String correlationId = event.getCorrelationId();
        Long orderId = event.getOrderId();

        log.info("[{}] 📥 Received PaymentSuccessEvent for order: {}", correlationId, orderId);

        // Simulate sending notification
        sendSuccessNotification(event.getCorrelationId(), event.getOrderId(), event.getAmount());

        log.info("[{}] ✅ Success notification sent for order: {}", correlationId, orderId);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.notificationservice.events.PaymentFailedEvent"
            }
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        String correlationId = event.getCorrelationId();
        Long orderId = event.getOrderId();

        log.info("[{}] 📥 Received PaymentFailedEvent for order: {}", correlationId, orderId);

        // Simulate sending failure notification
        sendFailureNotification(event.getCorrelationId(), event.getOrderId(), event.getReason());

        log.info("[{}] ✅ Failure notification sent for order: {}", correlationId, orderId);
    }

    private void sendSuccessNotification(String correlationId, Long orderId, Double amount) {
        log.info("[{}] 📧 Sending email: Order #{} confirmed! Amount: ${}", correlationId, orderId, amount);
        log.info("[{}] 📱 Sending SMS: Your order #{} has been successfully processed!", correlationId, orderId);
        // In real app: call email service, SMS service, push notification, etc.
    }

    private void sendFailureNotification(String correlationId, Long orderId, String reason) {
        log.info("[{}] 📧 Sending email: Order #{} failed. Reason: {}", correlationId, orderId, reason);
        log.info("[{}] 📱 Sending SMS: Unfortunately, order #{} could not be processed.", correlationId, orderId);
        // In real app: call notification services
    }
}