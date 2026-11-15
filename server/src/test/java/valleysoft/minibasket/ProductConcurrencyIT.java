package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import valleysoft.minibasket.product.repository.ProductRepository;

class ProductConcurrencyIT extends IntegrationTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private TransactionTemplate transaction;

    @Test
    void adminSaveFromAnOldCopy_afterACheckoutTookStock_isRefused_andStockStaysCorrect() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "ADMIN");
        String admin = login(email);
        long productId = createProduct("Phone", "100.00", 10);

        String loaded = mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andReturn().getResponse().getContentAsString();
        int loadedVersion = JsonPath.read(loaded, "$.version");
        long categoryId = ((Number) JsonPath.read(loaded, "$.category.id")).longValue();
        String sku = JsonPath.read(loaded, "$.sku");

        transaction.executeWithoutResult(status -> assertThat(productRepository.decreaseStock(productId, 3)).isEqualTo(1));
        assertThat(stockOf(productId)).isEqualTo(7);

        String form = """
                {"sku": "%s", "name": "Phone (renamed)", "price": 100.00, "stockQuantity": 10,
                 "categoryId": %d, "version": %d}
                """;
        mockMvc.perform(put("/api/v1/admin/products/{id}", productId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(form.formatted(sku, categoryId, loadedVersion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_CHANGED"));
        assertThat(stockOf(productId)).as("the sold items stay sold").isEqualTo(7);

        String reloaded = mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andReturn().getResponse().getContentAsString();
        int currentVersion = JsonPath.read(reloaded, "$.version");
        assertThat(currentVersion).isGreaterThan(loadedVersion);
        mockMvc.perform(put("/api/v1/admin/products/{id}", productId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"sku": "%s", "name": "Phone (renamed)", "price": 100.00, "stockQuantity": 7,
                                 "categoryId": %d, "version": %d}
                                """.formatted(sku, categoryId, currentVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone (renamed)"));
    }
}
