package com.elisiuslegodi.orders.dto;

import com.elisiuslegodi.orders.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, List<OrderItemResponse> items, BigDecimal totalPrice, Instant createdAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotalPrice(),
                order.getCreatedAt());
    }
}
