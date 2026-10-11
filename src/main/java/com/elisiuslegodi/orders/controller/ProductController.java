package com.elisiuslegodi.orders.controller;

import com.elisiuslegodi.orders.dto.CreateProductRequest;
import com.elisiuslegodi.orders.dto.PageResponse;
import com.elisiuslegodi.orders.dto.ProductResponse;
import com.elisiuslegodi.orders.service.ProductService;
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
@Tag(name = "Products")
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(product.id()).toUri();
        return ResponseEntity.created(location).body(product);
    }

    @Operation(summary = "Get a product by id")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @Operation(summary = "List products", description = "Paginated, ordered by id. Defaults to the first 20.")
    @GetMapping
    public PageResponse<ProductResponse> listProducts(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1-100")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return productService.listProducts(page, size);
    }
}
