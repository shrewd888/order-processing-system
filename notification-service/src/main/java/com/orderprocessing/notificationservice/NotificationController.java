package com.orderprocessing.notificationservice;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping
    public NotificationResponse sendNotification(@RequestBody NotificationRequest request) {
        System.out.println("📧 Sending notification to: " + request.getCustomerName());
        System.out.println("   Message: " + request.getMessage());

        // Simulate sending email/SMS
        return new NotificationResponse(true, "Notification sent");
    }
}