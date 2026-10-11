package com.elisiuslegodi.orders.service;

import com.elisiuslegodi.orders.domain.Order;
import com.elisiuslegodi.orders.domain.Product;
import com.elisiuslegodi.orders.dto.CreateOrderRequest;
import com.elisiuslegodi.orders.dto.OrderItemRequest;
import com.elisiuslegodi.orders.dto.OrderResponse;
import com.elisiuslegodi.orders.dto.PageResponse;
import com.elisiuslegodi.orders.exception.ProductsNotFoundException;
import com.elisiuslegodi.orders.exception.ResourceNotFoundException;
import com.elisiuslegodi.orders.repository.OrderRepository;
import com.elisiuslegodi.orders.repository.ProductRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Map<Long, Integer> quantities = mergeQuantitiesByProduct(request.items());

        Map<Long, Product> products = productRepository.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> missingIds = quantities.keySet().stream()
                .filter(id -> !products.containsKey(id))
                .sorted()
                .toList();
        if (!missingIds.isEmpty()) {
            throw new ProductsNotFoundException(missingIds);
        }

        Order order = new Order();
        quantities.forEach((productId, quantity) -> order.addItem(products.get(productId), quantity));
        Order saved = orderRepository.save(order);

        log.info("Order created: id={}, items={}, total={}",
                saved.getId(), saved.getItems().size(), saved.getTotalPrice());
        return OrderResponse.from(saved);
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public PageResponse<OrderResponse> listOrders(int page, int size) {
        return PageResponse.from(orderRepository.findAll(Pagination.byId(page, size)).map(OrderResponse::from));
    }

    private Map<Long, Integer> mergeQuantitiesByProduct(List<OrderItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        items.forEach(item -> quantities.merge(item.productId(), item.quantity(), Integer::sum));
        return quantities;
    }
}
