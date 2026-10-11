package com.elisiuslegodi.orders.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super("%s with id %d was not found".formatted(resource, id));
    }
}
