package com.elisiuslegodi.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elisiuslegodi.orders.domain.Order;
import com.elisiuslegodi.orders.domain.Product;
import com.elisiuslegodi.orders.dto.CreateOrderRequest;
import com.elisiuslegodi.orders.dto.OrderItemRequest;
import com.elisiuslegodi.orders.dto.OrderItemResponse;
import com.elisiuslegodi.orders.dto.OrderResponse;
import com.elisiuslegodi.orders.exception.ProductsNotFoundException;
import com.elisiuslegodi.orders.exception.ResourceNotFoundException;
import com.elisiuslegodi.orders.repository.OrderRepository;
import com.elisiuslegodi.orders.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderCalculatesTotalFromProductPricesAndQuantities() {
        givenExistingProducts(product(1L, "Keyboard", "499.99"), product(2L, "Mouse", "150.00"));
        givenSaveReturnsTheOrder();

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 1))));

        assertThat(response.totalPrice()).isEqualByComparingTo("1149.98");
        assertThat(response.items())
                .extracting(OrderItemResponse::productId, OrderItemResponse::quantity)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, 2),
                        org.assertj.core.groups.Tuple.tuple(2L, 1));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderDefaultsQuantityToOne() {
        givenExistingProducts(product(1L, "Keyboard", "499.99"));
        givenSaveReturnsTheOrder();

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, null))));

        assertThat(response.totalPrice()).isEqualByComparingTo("499.99");
    }

    @Test
    void createOrderMergesDuplicateProductsIntoOneLine() {
        givenExistingProducts(product(1L, "Keyboard", "499.99"));
        givenSaveReturnsTheOrder();

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, 1),
                new OrderItemRequest(1L, 2))));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.totalPrice()).isEqualByComparingTo("1499.97");
    }

    @Test
    void createOrderIsRejectedWhenAnyProductIsMissing() {
        givenExistingProducts(product(1L, "Keyboard", "499.99"));

        ProductsNotFoundException ex = catchThrowableOfType(ProductsNotFoundException.class,
                () -> orderService.createOrder(new CreateOrderRequest(List.of(
                        new OrderItemRequest(1L, 1),
                        new OrderItemRequest(99L, 1),
                        new OrderItemRequest(42L, 1)))));

        assertThat(ex.getMissingProductIds()).containsExactly(42L, 99L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order with id 7 was not found");
    }

    private void givenExistingProducts(Product... products) {
        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(products));
    }

    private void givenSaveReturnsTheOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Product product(Long id, String name, String price) {
        Product product = new Product(name, null, new BigDecimal(price));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
