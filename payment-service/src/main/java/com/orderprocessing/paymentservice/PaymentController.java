package com.orderprocessing.paymentservice;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        // Simulate payment processing
        System.out.println("Processing payment for order: " + request.getOrderId() +
                ", amount: $" + request.getAmount());

        // Simulate: payments under $1000 succeed, others fail
        boolean success = request.getAmount() < 1000;
        String message = success ? "Payment successful" : "Payment failed - amount too high";

        return new PaymentResponse(request.getOrderId(), success, message);
    }
}