package com.elisiuslegodi.orders.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elisiuslegodi.orders.repository.OrderRepository;
import com.elisiuslegodi.orders.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractApiIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    void resetState() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    protected static RequestPostProcessor validCredentials() {
        return httpBasic("admin", "admin123");
    }

    protected long createProduct(String name, String price) throws Exception {
        String body = mockMvc.perform(post("/api/v1/products")
                        .with(validCredentials())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "price": %s}
                                """.formatted(name, price)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.parse(body).read("$.id", Long.class);
    }
}
