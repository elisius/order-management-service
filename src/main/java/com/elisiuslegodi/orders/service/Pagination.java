package com.elisiuslegodi.orders.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class Pagination {
    private Pagination() {
    }

    static Pageable byId(int page, int size) {
        return PageRequest.of(page, size, Sort.by("id"));
    }
}
