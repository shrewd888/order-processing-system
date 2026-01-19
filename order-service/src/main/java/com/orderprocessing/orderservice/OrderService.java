package com.orderprocessing.orderservice;

import com.orderprocessing.orderservice.events.EventPublisher;
import com.orderprocessing.orderservice.events.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Value("${payment.service.url:http://payment-service:8080}")
    private String paymentServiceUrl;

    @Value("${inventory.service.url:http://inventory-service:8080}")
    private String inventoryServiceUrl;

    @Value("${notification.service.url:http://notification-service:8080}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Order processOrder(Order order) {
        // 1. Save initial order
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        System.out.println("🛒 Order created: " + order.getId());

        // Publish event instead of calling services directly
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount(),
                LocalDateTime.now().toString()
        );

        eventPublisher.publishOrderCreated(event);

        return order;
        // 2. Check inventory
//       InventoryRequest inventoryRequest = new InventoryRequest("PRODUCT-123", 1);
//        InventoryResponse inventoryResponse = restTemplate.postForObject(
//                inventoryServiceUrl + "/api/inventory/check",
//                inventoryRequest,
//                InventoryResponse.class
//        );
//
//        if (inventoryResponse == null || !inventoryResponse.isAvailable()) {
//            order.setStatus("FAILED - OUT OF STOCK");
//            return orderRepository.save(order);
//        }
//
//        System.out.println("✅ Inventory check passed");

        // 3. Process payment
//        PaymentRequest paymentRequest = new PaymentRequest(order.getId(), order.getTotalAmount());
//        PaymentResponse paymentResponse = restTemplate.postForObject(
//                paymentServiceUrl + "/api/payments",
//                paymentRequest,
//                PaymentResponse.class
//        );
//
//        if (paymentResponse == null || !paymentResponse.isSuccess()) {
//            order.setStatus("FAILED - PAYMENT DECLINED");
//            return orderRepository.save(order);
//        }
//
//        System.out.println("✅ Payment processed");

        // 4. Send notification
//        NotificationRequest notificationRequest = new NotificationRequest(
//                order.getId(),
//                order.getCustomerName(),
//                "Your order #" + order.getId() + " has been confirmed!"
//        );
//        restTemplate.postForObject(
//                notificationServiceUrl + "/api/notifications",
//                notificationRequest,
//                Object.class
//        );
//
//        System.out.println("✅ Notification sent");
//
//        // 5. Update order status
//        order.setStatus("CONFIRMED");
//        return orderRepository.save(order);
    }
}