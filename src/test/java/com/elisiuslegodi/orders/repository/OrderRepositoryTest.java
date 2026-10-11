package com.elisiuslegodi.orders.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.elisiuslegodi.orders.domain.Order;
import com.elisiuslegodi.orders.domain.Product;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class OrderRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReloadsOrderWithItems() {
        Product keyboard = productRepository.save(new Product("Keyboard", "Mechanical", new BigDecimal("499.99")));
        Product mouse = productRepository.save(new Product("Mouse", null, new BigDecimal("150.00")));

        Order order = new Order();
        order.addItem(keyboard, 2);
        order.addItem(mouse, 1);
        Long orderId = orderRepository.saveAndFlush(order).getId();
        entityManager.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();

        assertThat(reloaded.getTotalPrice()).isEqualByComparingTo("1149.98");
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getItems())
                .extracting(item -> item.getProduct().getId(), item -> item.getQuantity())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(keyboard.getId(), 2),
                        org.assertj.core.groups.Tuple.tuple(mouse.getId(), 1));
    }
}
