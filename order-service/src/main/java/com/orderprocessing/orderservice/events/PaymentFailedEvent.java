package com.orderprocessing.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent
{
    private String correlationId;
    private String eventId;
    private Long orderId;
    private String reason;
    private String timestamp;
}