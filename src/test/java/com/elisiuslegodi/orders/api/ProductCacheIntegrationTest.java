package com.elisiuslegodi.orders.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elisiuslegodi.orders.config.CacheConfig;
import com.elisiuslegodi.orders.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

class ProductCacheIntegrationTest extends AbstractApiIntegrationTest {
    @Test
    void productIsCachedAfterTheFirstRead() throws Exception {
        long id = createProduct("Keyboard", "499.99");
        assertThat(productCache().get(id)).isNull();

        mockMvc.perform(get("/api/v1/products/{id}", id).with(validCredentials())).andExpect(status().isOk());

        ProductResponse cached = productCache().get(id, ProductResponse.class);
        assertThat(cached).isNotNull();
        assertThat(cached.name()).isEqualTo("Keyboard");
    }

    @Test
    void unknownProductsAreNotCached() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 9999).with(validCredentials()))
                .andExpect(status().isNotFound());

        assertThat(productCache().get(9999L)).isNull();
    }

    private Cache productCache() {
        return cacheManager.getCache(CacheConfig.PRODUCTS);
    }
}
