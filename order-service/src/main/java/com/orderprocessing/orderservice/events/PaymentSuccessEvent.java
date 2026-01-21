package com.orderprocessing.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent
{
    private String correlationId;
    private Long orderId;
    private Double amount;
    private String timestamp;
}