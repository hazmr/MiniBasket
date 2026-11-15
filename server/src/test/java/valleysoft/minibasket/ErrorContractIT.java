package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import valleysoft.minibasket.common.exception.ErrorCode;

class ErrorContractIT extends IntegrationTest {

    @Test
    void everyError_hasATraceId_matchingTheResponseHeader() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/products/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.params.id").value(999999))
                .andReturn();

        String header = result.getResponse().getHeader("X-Request-Id");
        assertThat(header).isNotBlank();
        assertThat((String) JsonPath.read(result.getResponse().getContentAsString(), "$.traceId")).isEqualTo(header);
    }

    @Test
    void aRequestIdSentByTheCaller_isReused_butUnsafeOnesAreReplaced() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999999).header("X-Request-Id", "next-0123456789"))
                .andExpect(header().string("X-Request-Id", "next-0123456789"))
                .andExpect(jsonPath("$.traceId").value("next-0123456789"));

        MvcResult result = mockMvc.perform(get("/api/v1/products/{id}", 999999).header("X-Request-Id", "bad id\nFAKE LOG LINE"))
                .andReturn();
        assertThat(result.getResponse().getHeader("X-Request-Id")).doesNotContain("FAKE");
    }

    @Test
    void securityErrors_fromTheFilterChain_followTheSameContract() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void businessErrors_sendTheirValuesAsParams() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "CUSTOMER");
        long productId = createProduct("Last lamp", "20.00", 1);

        mockMvc.perform(post("/api/v1/cart/items").header("Authorization", login(email))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"productId": %d, "quantity": 5}
                                """.formatted(productId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.params.available").value(1))
                .andExpect(jsonPath("$.params.productName").value("Last lamp"));
    }

    @Test
    void validationErrors_sendTheConstraintAndItsLimits() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "not-an-email", "password": "short"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.violations[?(@.field == 'password')].code", hasItem("Size")))
                .andExpect(jsonPath("$.violations[?(@.field == 'password')].params.min", hasItem(8)))
                .andExpect(jsonPath("$.violations[?(@.field == 'password')].params.max", hasItem(72)))
                .andExpect(jsonPath("$.violations[?(@.field == 'email')].code", hasItem("Email")));
    }

    @Test
    void health_isPublic_metricsAreAdminOnly_andErrorsAreCountedByCode() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        String customer = uniqueEmail();
        createVerifiedUser(customer, "CUSTOMER");
        mockMvc.perform(get("/actuator/metrics").header("Authorization", login(customer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/brands/{id}/nothing", 1)).andExpect(status().isNotFound());
        String admin = uniqueEmail();
        createVerifiedUser(admin, "ADMIN");
        mockMvc.perform(get("/actuator/metrics/minibasket.api.errors").param("tag", "code:NOT_FOUND")
                        .header("Authorization", login(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(greaterThanOrEqualTo(1.0)));
    }

    @Test
    void theApiDocs_listEveryErrorCode() throws Exception {
        String[] allCodes = Stream.of(ErrorCode.values()).map(Enum::name).toArray(String[]::new);

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ErrorCode.enum", containsInAnyOrder(allCodes)))
                .andExpect(jsonPath("$.components.schemas.Problem.properties.errorCode").exists());
    }
}
