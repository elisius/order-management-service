package com.elisiuslegodi.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderTest {
    @Test
    void newOrderHasZeroTotal() {
        assertThat(new Order().getTotalPrice()).isEqualByComparingTo("0.00");
    }

    @Test
    void totalIsSumOfUnitPriceTimesQuantity() {
        Order order = new Order();

        order.addItem(new Product("Keyboard", null, new BigDecimal("499.99")), 2);
        order.addItem(new Product("Mouse", null, new BigDecimal("150.00")), 1);

        assertThat(order.getTotalPrice()).isEqualByComparingTo("1149.98");
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void itemKeepsThePriceAtTheTimeOfOrdering() {
        Order order = new Order();
        order.addItem(new Product("Keyboard", null, new BigDecimal("499.99")), 3);

        OrderItem item = order.getItems().get(0);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("499.99");
        assertThat(item.getLineTotal()).isEqualByComparingTo("1499.97");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        Order order = new Order();
        Product product = new Product("Keyboard", null, new BigDecimal("499.99"));

        assertThatThrownBy(() -> order.addItem(product, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
