package com.orderprocessing.paymentservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private boolean success;
    private String message;
}