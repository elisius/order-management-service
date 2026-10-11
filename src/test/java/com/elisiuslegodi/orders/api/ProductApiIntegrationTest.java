package com.elisiuslegodi.orders.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

class ProductApiIntegrationTest extends AbstractApiIntegrationTest {
    @Test
    void rejectsRequestsWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void rejectsRequestsWithWrongPassword() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(httpBasic("admin", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocsRequireAuthenticationToo() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v3/api-docs").with(validCredentials())).andExpect(status().isOk());
    }

    @Test
    void createsAProductAndReadsItBack() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(validCredentials())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Keyboard", "description": "Mechanical", "price": 499.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/products/" + onlyProductId())))
                .andExpect(jsonPath("$.id").value(onlyProductId()))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(499.99))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

        mockMvc.perform(get("/api/v1/products/{id}", onlyProductId()).with(validCredentials()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.description").value("Mechanical"));
    }

    @Test
    void createResponseMatchesWhatIsReadBackLater() throws Exception {
        String created = mockMvc.perform(post("/api/v1/products")
                        .with(validCredentials())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Mouse", "price": 150}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"price\":150.00")))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/v1/products/{id}", onlyProductId()).with(validCredentials()))
                .andExpect(content().json(created, JsonCompareMode.STRICT));
    }

    @Test
    void listsAllProducts() throws Exception {
        createProduct("Keyboard", "499.99");
        createProduct("Mouse", "150.00");

        mockMvc.perform(get("/api/v1/products").with(validCredentials()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void paginatesProducts() throws Exception {
        createProduct("Keyboard", "499.99");
        createProduct("Mouse", "150.00");
        createProduct("Monitor", "2999.00");

        mockMvc.perform(get("/api/v1/products?page=1&size=2").with(validCredentials()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Monitor"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void returns400ForOutOfRangePageSize() throws Exception {
        mockMvc.perform(get("/api/v1/products?size=1000").with(validCredentials()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WithFieldErrorsForInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(validCredentials())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": -5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name", notNullValue()))
                .andExpect(jsonPath("$.errors.price", notNullValue()));
    }

    @Test
    void returns404ForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 9999).with(validCredentials()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product with id 9999 was not found"));
    }

    @Test
    void everyApiResponseCarriesARequestId() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(validCredentials()))
                .andExpect(header().exists("X-Request-Id"));
    }

    private long onlyProductId() {
        return productRepository.findAll().get(0).getId();
    }
}
