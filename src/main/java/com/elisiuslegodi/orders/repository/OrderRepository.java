package com.elisiuslegodi.orders.repository;

import com.elisiuslegodi.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
