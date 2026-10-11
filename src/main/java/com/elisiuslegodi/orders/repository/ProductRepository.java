package com.elisiuslegodi.orders.repository;

import com.elisiuslegodi.orders.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
