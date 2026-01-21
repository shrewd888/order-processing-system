package com.orderprocessing.inventoryservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent
{
    private String correlationId;
    private String eventId;
    private Long orderId;
    private boolean reserved;
    private String timestamp;
}