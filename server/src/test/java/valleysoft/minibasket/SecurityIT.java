package valleysoft.minibasket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SecurityIT extends IntegrationTest {

    @Test
    void protectedEndpoint_withoutToken_returns401ProblemDetails() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void publicEndpoint_withoutToken_isAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk());
    }

    @Test
    void invalidToken_isRejectedEvenOnPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/products").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void adminEndpoint_asCustomer_returns403() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "CUSTOMER");

        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", login(email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void adminEndpoint_asAdmin_isAllowed() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "ADMIN");

        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", login(email)))
                .andExpect(status().isOk());
    }

    @Test
    void customer_cannotSeeAnotherCustomersOrder() throws Exception {
        long ownerId = createVerifiedUser(uniqueEmail(), "CUSTOMER");
        long orderId = jdbc.queryForObject("""
                insert into orders (order_number, user_id, shipping_method_id, status, payment_status,
                                    subtotal, discount_amount, shipping_fee, total, shipping_street, shipping_city)
                values (?, ?, ?, 'PENDING', 'UNPAID', 10, 0, 5, 15, 'Street', 'City')
                returning id
                """, Long.class, "MB-" + UUID.randomUUID().toString().substring(0, 8), ownerId, createShippingMethod("5.00"));

        String otherCustomer = uniqueEmail();
        createVerifiedUser(otherCustomer, "CUSTOMER");

        mockMvc.perform(get("/api/v1/orders/{id}", orderId).header("Authorization", login(otherCustomer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }

    @Test
    void invalidRequestBody_returns400WithFieldViolations() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "not-an-email", "password": "short"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.violations.length()").value(2));
    }

    @Test
    void unknownSortField_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"));
    }

    @Test
    void malformedJson_returns400WithErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }
}
