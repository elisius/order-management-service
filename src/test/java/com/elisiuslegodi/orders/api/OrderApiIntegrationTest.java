package com.elisiuslegodi.orders.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class OrderApiIntegrationTest extends AbstractApiIntegrationTest {
    @Test
    void rejectsRequestsWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": []}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsAnOrderWithCalculatedTotalAndPersistsIt() throws Exception {
        long keyboard = createProduct("Keyboard", "499.99");
        long mouse = createProduct("Mouse", "150.00");

        String body = placeOrder("""
                {"items": [{"productId": %d, "quantity": 2}, {"productId": %d, "quantity": 1}]}
                """.formatted(keyboard, mouse))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.totalPrice").value(1149.98))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(499.99))
                .andExpect(jsonPath("$.items[0].lineTotal").value(999.98))
                .andReturn().getResponse().getContentAsString();
        long orderId = JsonPath.parse(body).read("$.id", Long.class);

        assertThat(orderRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId).with(validCredentials()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.totalPrice").value(1149.98))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void rejectsOrderAndPersistsNothingWhenAProductIsMissing() throws Exception {
        long keyboard = createProduct("Keyboard", "499.99");

        placeOrder("""
                {"items": [{"productId": %d, "quantity": 1}, {"productId": 987654, "quantity": 1}]}
                """.formatted(keyboard))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Order rejected"))
                .andExpect(jsonPath("$.missingProductIds", contains(987654)));

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void mergesDuplicateProductsAndDefaultsQuantityToOne() throws Exception {
        long keyboard = createProduct("Keyboard", "100.00");

        placeOrder("""
                {"items": [{"productId": %d}, {"productId": %d, "quantity": 2}]}
                """.formatted(keyboard, keyboard))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(300.00));
    }

    @Test
    void returns400ForOrderWithoutItems() throws Exception {
        placeOrder("{\"items\": []}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void returns400ForNonPositiveQuantity() throws Exception {
        long keyboard = createProduct("Keyboard", "100.00");

        placeOrder("""
                {"items": [{"productId": %d, "quantity": 0}]}
                """.formatted(keyboard))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404ForUnknownOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 9999).with(validCredentials()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Order with id 9999 was not found"));
    }

    @Test
    void listsOrdersInPages() throws Exception {
        long keyboard = createProduct("Keyboard", "100.00");
        placeOrder("{\"items\": [{\"productId\": %d}]}".formatted(keyboard)).andExpect(status().isCreated());
        placeOrder("{\"items\": [{\"productId\": %d}]}".formatted(keyboard)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/orders").with(validCredentials()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/orders?page=0&size=1").with(validCredentials()))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    private ResultActions placeOrder(String json) throws Exception {
        return mockMvc.perform(post("/api/v1/orders")
                .with(validCredentials())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
