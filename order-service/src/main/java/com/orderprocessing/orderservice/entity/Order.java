package com.orderprocessing.orderservice.entity;

import com.orderprocessing.orderservice.model.OrderState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private BigDecimal totalAmount;
    /**
     * Store as string in database
     * Changed from 'status' to 'state'
     */
    @Enumerated(EnumType.STRING)
    private OrderState state;

    // Constructor for creating new orders
    public Order(String customerName, BigDecimal totalAmount) {
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.state = OrderState.PENDING;  // Always start in PENDING
    }
}