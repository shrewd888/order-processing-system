package com.orderprocessing.orderservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationRequest {
    private Long orderId;
    private String customerName;
    private String message;
}