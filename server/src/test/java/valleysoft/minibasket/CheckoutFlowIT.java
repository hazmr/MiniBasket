package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CheckoutFlowIT extends IntegrationTest {

    @Test
    void customer_canRegisterVerifyLoginAndCheckOut() throws Exception {
        long productId = createProduct("Phone", "100.00", 5);
        long shippingMethodId = createShippingMethod("5.00");
        String couponCode = "SAVE" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        jdbc.update("insert into coupons (code, discount_type, discount_value) values (?, 'PERCENTAGE', 10)", couponCode);
        String email = uniqueEmail();

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailVerified").value(false));

        String token = jdbc.queryForObject("""
                select t.token from verification_tokens t join users u on u.id = t.user_id
                where u.email = ? and t.type = 'EMAIL_VERIFICATION'
                """, String.class, email);
        mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content("""
                        {"token": "%s"}
                        """.formatted(token)))
                .andExpect(status().isNoContent());

        String auth = login(email);
        String address = mockMvc.perform(post("/api/v1/addresses").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"street": "12 Olive St", "city": "Amman", "defaultAddress": true}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int addressId = JsonPath.read(address, "$.id");

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"productId": %d, "quantity": 2}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(200.00));

        String order = mockMvc.perform(post("/api/v1/orders").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"addressId": %d, "shippingMethodId": %d, "couponCode": "%s"}
                                """.formatted(addressId, shippingMethodId, couponCode.toLowerCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.discountAmount").value(20.00))
                .andExpect(jsonPath("$.total").value(185.00))
                .andReturn().getResponse().getContentAsString();
        int orderId = JsonPath.read(order, "$.id");

        mockMvc.perform(post("/api/v1/orders/{id}/pay", orderId).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        assertThat(stockOf(productId)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from cart_items c join users u on u.id = c.user_id where u.email = ?",
                Integer.class, email)).isZero();
        assertThat(jdbc.queryForObject("select total from orders where id = ?", BigDecimal.class, orderId))
                .isEqualByComparingTo("185.00");
    }

    @Test
    void checkout_withMoreThanStock_isRejectedAndTakesNoStock() throws Exception {
        long productId = createProduct("Last phone", "100.00", 1);
        long shippingMethodId = createShippingMethod("5.00");
        String email = uniqueEmail();
        long userId = createVerifiedUser(email, "CUSTOMER");
        long addressId = jdbc.queryForObject(
                "insert into addresses (user_id, street, city) values (?, 'Street', 'City') returning id", Long.class, userId);
        jdbc.update("insert into cart_items (user_id, product_id, quantity) values (?, ?, 2)", userId, productId);

        mockMvc.perform(post("/api/v1/orders").header("Authorization", login(email))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"addressId": %d, "shippingMethodId": %d}
                                """.formatted(addressId, shippingMethodId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

        assertThat(stockOf(productId)).isEqualTo(1);
        mockMvc.perform(get("/api/v1/orders").header("Authorization", login(email)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
