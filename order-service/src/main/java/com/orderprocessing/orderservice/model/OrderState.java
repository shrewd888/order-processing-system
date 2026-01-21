package com.orderprocessing.orderservice.model;

public enum OrderState {
    PENDING,      // Order created, waiting for processing
    PROCESSING,   // Inventory reserved, payment in progress
    CONFIRMED,    // Payment successful, order complete
    FAILED,       // Payment failed or inventory unavailable
    CANCELLED     // User cancelled the order
}