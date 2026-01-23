package com.orderprocessing.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://order-service:8080"))
                .route("payment-service", r -> r
                        .path("/payments/**")
                        .uri("http://payment-service:8080"))
                .route("inventory-service", r -> r
                        .path("/inventory/**")
                        .uri("http://inventory-service:8080"))
                .route("notification-service", r -> r
                        .path("/notifications/**")
                        .uri("http://notification-service:8080"))
                .build();
    }
}