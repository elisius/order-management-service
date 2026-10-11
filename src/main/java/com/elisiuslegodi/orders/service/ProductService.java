package com.elisiuslegodi.orders.service;

import com.elisiuslegodi.orders.domain.Product;
import com.elisiuslegodi.orders.dto.CreateProductRequest;
import com.elisiuslegodi.orders.dto.ProductResponse;
import com.elisiuslegodi.orders.exception.ResourceNotFoundException;
import com.elisiuslegodi.orders.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = productRepository.save(
                new Product(request.name().trim(), request.description(), request.price()));
        return ProductResponse.from(product);
    }

    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }
}
