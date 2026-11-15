package valleysoft.minibasket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestInfrastructure.class)
public abstract class IntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired private PasswordEncoder passwordEncoder;

    protected static final String PASSWORD = "secret123";

    protected static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    protected long createVerifiedUser(String email, String roleName) {
        return jdbc.queryForObject("""
                insert into users (role_id, email, password, email_verified)
                values ((select id from roles where name = ?), ?, ?, true)
                returning id
                """, Long.class, roleName, email, passwordEncoder.encode(PASSWORD));
    }

    protected String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    protected long createProduct(String name, String price, int stock) {
        long categoryId = jdbc.queryForObject(
                "insert into categories (name) values ('Test category') returning id", Long.class);
        return jdbc.queryForObject("""
                insert into products (category_id, sku, name, price, stock_quantity)
                values (?, ?, ?, ?, ?)
                returning id
                """, Long.class, categoryId, "SKU-" + UUID.randomUUID(), name, new BigDecimal(price), stock);
    }

    protected long createShippingMethod(String fee) {
        return jdbc.queryForObject("insert into shipping_methods (name, fee) values ('Standard', ?) returning id",
                Long.class, new BigDecimal(fee));
    }

    protected int stockOf(long productId) {
        return jdbc.queryForObject("select stock_quantity from products where id = ?", Integer.class, productId);
    }
}
