package com.orderprocessing.inventoryservice;

import lombok.Data;

@Data
public class InventoryRequest {
    private String productId;
    private Integer quantity;
}