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
    public void handleInventoryReserved(@Payload InventoryReservedEvent event)
    {
        String correlationId = event.getCorrelationId();
        Long orderId = event.getOrderId();
        String eventId = event.getEventId();

        log.info("[{}] 📥 Received InventoryReservedEvent for order: {}", correlationId, orderId);

        // Idempotency check
        if (processedEventRepository.existsById(eventId)) {
            log.warn("[{}] 🔁 Duplicate InventoryReservedEvent detected: {}. Skipping.", correlationId, eventId);
            return;
        }

        if (!event.isReserved()) {
            log.warn("[{}] ⚠️ Inventory not reserved, skipping payment for order: {}", correlationId, orderId);
            return;
        }

        // Simulate payment processing
        // For demo: orders with even IDs succeed, odd IDs fail
        try {
            boolean paymentSuccess = processPayment(correlationId, orderId);
            //Mark as processed AFTER successful processing
            saveProcessedEvent(correlationId, orderId, eventId, "InventoryReservedEvent");

            if (paymentSuccess) {
                log.info("[{}] ✅ Payment successful for order: {}", correlationId, orderId);

                PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
                        correlationId,
                        orderId,
                        100.0, // Simulated amount
                        LocalDateTime.now().toString()
                );
                eventPublisher.publishPaymentSuccess(successEvent);

            } else {
                log.error("[{}] ❌ Payment failed for order: {}", correlationId, orderId);

                PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                        correlationId,
                        UUID.randomUUID().toString(),
                        orderId,
                        "Insufficient funds", // Simulated reason
                        LocalDateTime.now().toString()
                );
                eventPublisher.publishPaymentFailed(failedEvent);
            }
        }
        catch (Exception e)
        {
            log.error("[{}] ❌ Error processing InventoryReservedEvent for order {}: {}",
                    correlationId, orderId, e.getMessage());
            throw e;  // Don't mark as processed on failure
        }
    }

    private boolean processPayment(String correlationId, Long orderId) {
        log.info("[{}] 💳 Processing payment for order: {}", correlationId, orderId);

        // Simulate payment logic:
        // Even order IDs = success
        // Odd order IDs = failure (to test compensation!)
        boolean success = (orderId % 2 == 0);

        if (success) {
            log.info("[{}] 💰 Payment charged successfully for order {}", correlationId, orderId);
        } else {
            log.error("[{}] 💸 Payment declined for order {} - insufficient funds", correlationId, orderId);
        }

        return success;
    }

    private void saveProcessedEvent(String correlationId, Long orderId, String eventId, String eventType) {
        ProcessedEvent processed = new ProcessedEvent(
                eventId,
                orderId,
                eventType,
                LocalDateTime.now(),
                "payment-service"
        );
        processedEventRepository.save(processed);
        log.info("[{}] ✅ Marked order {} event {} as processed", correlationId, orderId, eventId);
    }
}