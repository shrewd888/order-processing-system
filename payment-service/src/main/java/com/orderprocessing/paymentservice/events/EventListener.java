package com.orderprocessing.paymentservice.events;

import com.orderprocessing.paymentservice.entity.ProcessedEvent;
import com.orderprocessing.paymentservice.repository.ProcessedEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    //constructor injection for 'final', created by @RequiredArgsConstructor
    private final EventPublisher eventPublisher;

    //field injection
//    @Autowired
//    private ProcessedEventRepository processedEventRepository;

    private final ProcessedEventRepository processedEventRepository;

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
    @Transactional
    public void handleInventoryReserved(@Payload InventoryReservedEvent event) {
        log.info("📥 Received InventoryReservedEvent: {}", event);
        Long orderId = event.getOrderId();
        String eventId = event.getEventId();

        // ← ADD IDEMPOTENCY CHECK
        if (processedEventRepository.existsById(eventId)) {
            log.warn("🔁 Duplicate InventoryReservedEvent detected: {}. Skipping.", eventId);
            return;
        }

        if (!event.isReserved()) {
            log.warn("⚠️ Inventory not reserved, skipping payment for order: {}", event.getOrderId());
            return;
        }

        // Simulate payment processing
        // For demo: orders with even IDs succeed, odd IDs fail
        try {
            boolean paymentSuccess = processPayment(event.getOrderId());
            // ← Mark as processed AFTER successful processing
            saveProcessedEvent(orderId, eventId, "InventoryReservedEvent");

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
                        UUID.randomUUID().toString(),
                        event.getOrderId(),
                        "Insufficient funds", // Simulated reason
                        LocalDateTime.now().toString()
                );
                eventPublisher.publishPaymentFailed(failedEvent);
            }
        }
        catch (Exception e)
        {
            log.error("❌ Error processing InventoryReservedEvent for order {}: {}",
                    event.getOrderId(), e.getMessage());
            throw e;  // Don't mark as processed on failure
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

    private void saveProcessedEvent(Long orderId, String eventId, String eventType) {
        ProcessedEvent processed = new ProcessedEvent(
                eventId,
                orderId,
                eventType,
                LocalDateTime.now(),
                "payment-service"
        );
        processedEventRepository.save(processed);
        log.info("✅ Marked event {} as processed", eventId);
    }
}