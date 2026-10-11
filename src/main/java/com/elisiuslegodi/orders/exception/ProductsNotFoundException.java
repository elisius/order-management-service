package com.elisiuslegodi.orders.exception;

import java.util.List;

public class ProductsNotFoundException extends RuntimeException {
    private final List<Long> missingProductIds;

    public ProductsNotFoundException(List<Long> missingProductIds) {
        super("Order rejected: products not found for ids " + missingProductIds);
        this.missingProductIds = List.copyOf(missingProductIds);
    }

    public List<Long> getMissingProductIds() {
        return missingProductIds;
    }
}
