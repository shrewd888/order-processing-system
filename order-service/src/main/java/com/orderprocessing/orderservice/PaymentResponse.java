package com.orderprocessing.orderservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private boolean success;
    private String message;
}