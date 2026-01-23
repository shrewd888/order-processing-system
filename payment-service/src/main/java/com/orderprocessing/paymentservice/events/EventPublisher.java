package com.orderprocessing.paymentservice.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(PaymentSuccessEvent event)
    {
        String correlationId = event.getCorrelationId();
        String key = event.getOrderId().toString();  // Use orderId as key
        log.info("[{}] 💳 Publishing PaymentSuccessEvent with key {}: {}", correlationId, key, event);
        kafkaTemplate.send("payment-success", key, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event)
    {
        String correlationId = event.getCorrelationId();
        String key = event.getOrderId().toString();  // Use orderId as key
        log.error("[{}] 💥 Publishing PaymentFailedEvent with key {}: {}", correlationId, key, event);
        kafkaTemplate.send("payment-failed", key, event);
    }
}