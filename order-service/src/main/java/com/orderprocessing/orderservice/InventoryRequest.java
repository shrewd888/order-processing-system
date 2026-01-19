package com.orderprocessing.orderservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryRequest {
    private String productId;
    private Integer quantity;
}