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
        log.info("[{}] 📥 Received PaymentSuccessEvent for order {}", event.getCorrelationId(), event.getOrderId());

        try
        {
            orderService.handlePaymentSuccess(event.getCorrelationId(), event.getOrderId());
        }
        catch(IllegalStateException e)
        {
            log.error("[{}] ❌ State transition error: {}",
                    event.getCorrelationId(), e.getMessage());
        }
        catch (Exception e)
        {
            log.error("[{}] ❌ Error handling PaymentSuccessEvent: {}",
                    event.getCorrelationId(), e.getMessage(), e);
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
        log.info("[{}] 📥 Received PaymentFailedEvent for order: {}", event.getCorrelationId(), event.getOrderId());

        try
        {
            orderService.handlePaymentFailure(event.getCorrelationId(), event.getOrderId());
        }
        catch (IllegalStateException e)
        {
            log.error("[{}] ❌ State transition error: {}",
                    event.getCorrelationId(), e.getMessage());
        }
        catch (Exception e)
        {
            log.error("[{}] ❌ Error handling PaymentFailedEvent: {}",
                    event.getCorrelationId(), e.getMessage(), e);
        }
    }
}