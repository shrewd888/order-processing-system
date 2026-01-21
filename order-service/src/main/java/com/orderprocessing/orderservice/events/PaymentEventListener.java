package com.orderprocessing.orderservice.events;

import com.orderprocessing.orderservice.OrderRepository;
import com.orderprocessing.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentEventListener {

    @Autowired
    private OrderService orderService;

    @KafkaListener(
            topics = "payment-success",
            groupId = "order-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.orderservice.events.PaymentSuccessEvent"
            }
    )
    public void handlePaymentSuccess(@Payload PaymentSuccessEvent event)
    {
        log.info("📥 Received PaymentSuccessEvent for order {}", event.getOrderId());

        try
        {
            orderService.handlePaymentSuccess(event.getOrderId());
        }
        catch(IllegalStateException e)
        {
            log.error("handlePaymentSuccess state transition error: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "spring.json.value.default.type=com.orderprocessing.orderservice.events.PaymentFailedEvent"
            }
    )
    public void handlePaymentFailed(@Payload PaymentFailedEvent event) {
        log.info("📥 Received PaymentFailedEvent for order: {}", event.getOrderId());

        try
        {
            orderService.handlePaymentFailure(event.getOrderId());
        }
        catch (IllegalStateException e)
        {
            log.error("State transition error: {}", e.getMessage());
        }
    }
}