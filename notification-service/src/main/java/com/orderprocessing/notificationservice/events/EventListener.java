package com.orderprocessing.notificationservice.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

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
        log.info("📥 Received PaymentSuccessEvent: {}", event);

        // Simulate sending notification
        sendSuccessNotification(event.getOrderId(), event.getAmount());

        log.info("✅ Success notification sent for order: {}", event.getOrderId());
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
        log.info("📥 Received PaymentFailedEvent: {}", event);

        // Simulate sending failure notification
        sendFailureNotification(event.getOrderId(), event.getReason());

        log.info("✅ Failure notification sent for order: {}", event.getOrderId());
    }

    private void sendSuccessNotification(Long orderId, Double amount) {
        log.info("📧 Sending email: Order #{} confirmed! Amount: ${}", orderId, amount);
        log.info("📱 Sending SMS: Your order #{} has been successfully processed!", orderId);
        // In real app: call email service, SMS service, push notification, etc.
    }

    private void sendFailureNotification(Long orderId, String reason) {
        log.info("📧 Sending email: Order #{} failed. Reason: {}", orderId, reason);
        log.info("📱 Sending SMS: Unfortunately, order #{} could not be processed.", orderId);
        // In real app: call notification services
    }
}