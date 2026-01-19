package com.orderprocessing.inventoryservice.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryReserved(InventoryReservedEvent event)
    {
        String key = event.getOrderId().toString();

        log.info("📦 Publishing InventoryReservedEvent with key {}: {}", key, event);

        kafkaTemplate.send("inventory-reserved", key, event);
    }
}