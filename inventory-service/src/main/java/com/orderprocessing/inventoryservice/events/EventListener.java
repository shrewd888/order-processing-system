package com.orderprocessing.inventoryservice.events;

import com.orderprocessing.inventoryservice.entity.ProcessedEvent;
import com.orderprocessing.inventoryservice.repository.ProcessedEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    private final EventPublisher eventPublisher;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "order-created",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.inventoryservice.events.OrderCreatedEvent"
            })
    @Transactional  // Important: ensures atomicity
    public void handleOrderCreated(@Payload OrderCreatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition)
    {
        String correlationId = event.getCorrelationId();
        String eventId = event.getEventId();
        Long orderId = event.getOrderId();

        log.info("[{}] 📥 Received OrderCreatedEvent from partition {}: {}", correlationId, partition, orderId);

        // Step 1: Check if already processed
        if (processedEventRepository.existsById(eventId)) {
            log.warn("[{}] 🔁 Duplicate detected: {}. Skipping.", correlationId, eventId);
            return;  // ← EXIT EARLY, don't process again
        }

        try {
            // Simulate inventory reservation logic
            boolean reserved = reserveInventory(correlationId, orderId);

            if (reserved) {
                log.info("[{}] ✅ Inventory reserved for order: {}", correlationId, orderId);
            } else {
                log.warn("[{}] ❌ Inventory reservation failed for order: {}", correlationId, orderId);
            }

            saveProcessedEvent(correlationId, orderId, eventId, "OrderCreatedEvent");

            // Publish InventoryReserved event
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    correlationId,
                    UUID.randomUUID().toString(),
                    event.getOrderId(),
                    reserved,
                    LocalDateTime.now().toString()
            );
            eventPublisher.publishInventoryReserved(reservedEvent);
        }
        catch (Exception e)
        {
            log.error("[{}] ❌ Error processing OrderCreatedEvent for order {}: {}",
                    correlationId, orderId, e.getMessage());
            throw e; // Don't mark as processed on failure - allow retry
        }
    }

    @KafkaListener(topics = "payment-failed",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.inventoryservice.events.PaymentFailedEvent"
            })
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event)
    {
        String correlationId = event.getCorrelationId();
        String eventId = event.getEventId();
        Long orderId = event.getOrderId();

        log.info("[{}] 📥 Received PaymentFailedEvent: {}", correlationId, orderId);
        log.warn("[{}] 💥 COMPENSATION: Releasing inventory for order: {}", correlationId, orderId);


        if (processedEventRepository.existsById(eventId))
        {
            log.warn("[{}] 🔁 Duplicate PaymentFailedEvent detected: {}. Skipping.", correlationId, eventId);
            return;
        }
        try {
            // Release the reserved inventory (compensation logic!)
            releaseInventory(correlationId, orderId);
            // Mark as processed
            saveProcessedEvent(correlationId, orderId, eventId, "PaymentFailedEvent");

            log.info("[{}] ✅ Inventory released for order: {}", correlationId, orderId);
        }
        catch (Exception e) {
            log.error("[{}] ❌ Error releasing inventory for order {}: {}",
                    correlationId, orderId, e.getMessage());
            throw e;
        }
    }

    private boolean reserveInventory(String correlationId, Long orderId) {
        // Simulate inventory check - always succeed for now
        log.info("[{}] 🔒 Reserving inventory for order: {}", correlationId, orderId);
        return true;
    }

    private void releaseInventory(String correlationId, Long orderId) {
        // Simulate inventory release - compensation action
        log.info("[{}] 🔓 Releasing inventory for order: {}", correlationId, orderId);
    }

    private void saveProcessedEvent(String correlationId, Long orderId, String eventId, String eventType) {
        ProcessedEvent processed = new ProcessedEvent(
                eventId,
                orderId,
                eventType,
                LocalDateTime.now(),
                "inventory-service"
        );
        processedEventRepository.save(processed);
        log.info("[{}] ✅ Marked event {} as processed", correlationId, eventId);
    }
}