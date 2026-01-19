package com.orderprocessing.notificationservice;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long orderId;
    private String customerName;
    private String message;
}