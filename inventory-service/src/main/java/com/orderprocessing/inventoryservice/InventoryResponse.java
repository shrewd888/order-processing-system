package com.orderprocessing.inventoryservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryResponse {
    private boolean available;
    private String message;
}