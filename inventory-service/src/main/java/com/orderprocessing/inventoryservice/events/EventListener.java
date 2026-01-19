package com.orderprocessing.inventoryservice.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    @KafkaListener(topics = "order-created",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.inventoryservice.events.OrderCreatedEvent"
            })
    public void handleOrderCreated(@Payload OrderCreatedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition)
    {
        log.info("📥 Received OrderCreatedEvent from partition {}: {}", partition, event);

        // Simulate inventory reservation logic
        boolean reserved = reserveInventory(event.getOrderId());

        if (reserved) {
            log.info("✅ Inventory reserved for order: {}", event.getOrderId());
        } else {
            log.warn("❌ Inventory reservation failed for order: {}", event.getOrderId());
        }

        // Publish InventoryReserved event
        InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                event.getOrderId(),
                reserved,
                LocalDateTime.now().toString()
        );

        eventPublisher.publishInventoryReserved(reservedEvent);
    }

    @KafkaListener(topics = "payment-failed",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.inventoryservice.events.PaymentFailedEvent"
            })
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("📥 Received PaymentFailedEvent: {}", event);
        log.warn("💥 COMPENSATION: Releasing inventory for order: {}", event.getOrderId());

        // Release the reserved inventory (compensation logic!)
        releaseInventory(event.getOrderId());

        log.info("✅ Inventory released for order: {}", event.getOrderId());
    }

    private boolean reserveInventory(Long orderId) {
        // Simulate inventory check - always succeed for now
        log.info("🔒 Reserving inventory for order: {}", orderId);
        return true;
    }

    private void releaseInventory(Long orderId) {
        // Simulate inventory release - compensation action
        log.info("🔓 Releasing inventory for order: {}", orderId);
    }
}