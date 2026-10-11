package com.elisiuslegodi.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elisiuslegodi.orders.domain.Product;
import com.elisiuslegodi.orders.dto.CreateProductRequest;
import com.elisiuslegodi.orders.dto.ProductResponse;
import com.elisiuslegodi.orders.exception.ResourceNotFoundException;
import com.elisiuslegodi.orders.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProductSavesAndReturnsTheProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(
                new CreateProductRequest("  Keyboard ", "Mechanical", new BigDecimal("499.99")));

        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Keyboard");
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualByComparingTo("499.99");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void getProductReturnsExistingProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(new Product("Mouse", null, new BigDecimal("150.00"))));

        assertThat(productService.getProduct(1L).name()).isEqualTo("Mouse");
    }

    @Test
    void getProductThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product with id 42 was not found");
    }

    @Test
    void listProductsMapsEveryProduct() {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Keyboard", null, new BigDecimal("499.99")),
                new Product("Mouse", null, new BigDecimal("150.00"))));

        assertThat(productService.listProducts())
                .extracting(ProductResponse::name)
                .containsExactly("Keyboard", "Mouse");
    }
}
