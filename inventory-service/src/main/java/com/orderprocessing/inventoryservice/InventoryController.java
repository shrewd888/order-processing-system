package com.orderprocessing.inventoryservice;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @PostMapping("/check")
    public InventoryResponse checkInventory(@RequestBody InventoryRequest request) {
        System.out.println("Checking inventory for product: " + request.getProductId() +
                ", quantity: " + request.getQuantity());

        // Simulate: always available for now
        return new InventoryResponse(true, "Items in stock");
    }
}