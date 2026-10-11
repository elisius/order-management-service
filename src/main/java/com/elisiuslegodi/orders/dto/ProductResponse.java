package com.elisiuslegodi.orders.dto;

import com.elisiuslegodi.orders.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String name, String description, BigDecimal price, Instant createdAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt());
    }
}
