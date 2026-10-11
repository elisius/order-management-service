package com.elisiuslegodi.orders.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long productId,
        @Min(1) @Max(10_000) Integer quantity) {
    public OrderItemRequest {
        if (quantity == null) {
            quantity = 1;
        }
    }
}
