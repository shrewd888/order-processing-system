package com.orderprocessing.orderservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private Double amount;
}
