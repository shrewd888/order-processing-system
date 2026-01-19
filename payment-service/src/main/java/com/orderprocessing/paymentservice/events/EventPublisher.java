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

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        log.info("💳 Publishing PaymentSuccessEvent: {}", event);
        kafkaTemplate.send("payment-success", event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.error("💥 Publishing PaymentFailedEvent: {}", event);
        kafkaTemplate.send("payment-failed", event);
    }
}