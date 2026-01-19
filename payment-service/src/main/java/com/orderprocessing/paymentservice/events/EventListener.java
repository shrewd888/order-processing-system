package com.orderprocessing.paymentservice.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final EventPublisher eventPublisher;
    // In a real app, we'd get the order amount from a database or the event
    // For now, we'll simulate based on orderId

    @KafkaListener(
            topics = "inventory-reserved",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.paymentservice.events.InventoryReservedEvent"
            }
    )
    public void handleInventoryReserved(@Payload InventoryReservedEvent event) {
        log.info("📥 Received InventoryReservedEvent: {}", event);

        if (!event.isReserved()) {
            log.warn("⚠️ Inventory not reserved, skipping payment for order: {}", event.getOrderId());
            return;
        }

        // Simulate payment processing
        // For demo: orders with even IDs succeed, odd IDs fail
        boolean paymentSuccess = processPayment(event.getOrderId());

        if (paymentSuccess) {
            log.info("✅ Payment successful for order: {}", event.getOrderId());

            PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
                    event.getOrderId(),
                    100.0, // Simulated amount
                    LocalDateTime.now().toString()
            );
            eventPublisher.publishPaymentSuccess(successEvent);

        } else {
            log.error("❌ Payment failed for order: {}", event.getOrderId());

            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    event.getOrderId(),
                    "Insufficient funds", // Simulated reason
                    LocalDateTime.now().toString()
            );
            eventPublisher.publishPaymentFailed(failedEvent);
        }
    }

    private boolean processPayment(Long orderId) {
        log.info("💳 Processing payment for order: {}", orderId);

        // Simulate payment logic:
        // Even order IDs = success
        // Odd order IDs = failure (to test compensation!)
        boolean success = (orderId % 2 == 0);

        if (success) {
            log.info("💰 Payment charged successfully");
        } else {
            log.error("💸 Payment declined - insufficient funds");
        }

        return success;
    }
}