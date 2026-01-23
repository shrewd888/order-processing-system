package com.orderprocessing.orderservice.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event)
    {
        String correlationId = event.getCorrelationId();
        String key = event.getOrderId().toString();  // Use orderId as key

        log.info("[{}] 📤 Publishing OrderCreatedEvent with key {}: {}", correlationId, key, event);

        kafkaTemplate.send("order-created", key, event);

        log.debug("[{}] ✅ Event sent to partition determined by key: {}", correlationId, key);
    }
}