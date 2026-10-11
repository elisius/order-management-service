package com.elisiuslegodi.orders.controller;

import com.elisiuslegodi.orders.dto.CreateOrderRequest;
import com.elisiuslegodi.orders.dto.PageResponse;
import com.elisiuslegodi.orders.dto.OrderResponse;
import com.elisiuslegodi.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Tag(name = "Orders")
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create an order",
            description = "All product ids must exist. The total price is calculated by the service.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    @ApiResponse(responseCode = "422", description = "One or more products do not exist", content = @Content)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(order.id()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @Operation(summary = "Get an order by id")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @Operation(summary = "List orders", description = "Paginated, ordered by id. Defaults to the first 20.")
    @GetMapping
    public PageResponse<OrderResponse> listOrders(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1-100")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return orderService.listOrders(page, size);
    }
}
