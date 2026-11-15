package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import valleysoft.minibasket.category.dto.CategoryResponse;
import valleysoft.minibasket.common.cache.CacheNames;

class RedisIT extends IntegrationTest {

    private static final String CATEGORIES_KEY = "minibasket:categories:all";

    @Autowired private StringRedisTemplate redis;
    @Autowired private CacheManager cacheManager;

    @Test
    void categoryList_isCachedWithATtl_andComesBackAsRecords() throws Exception {
        redis.delete(CATEGORIES_KEY);

        mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());

        assertThat(redis.getExpire(CATEGORIES_KEY)).isBetween(Duration.ofMinutes(9).toSeconds(), Duration.ofMinutes(11).toSeconds());
        List<?> cached = cacheManager.getCache(CacheNames.CATEGORIES).get("all", List.class);
        assertThat(cached).isNotEmpty().allMatch(CategoryResponse.class::isInstance);
    }

    @Test
    void creatingACategory_evictsTheCachedList() throws Exception {
        mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());
        assertThat(redis.hasKey(CATEGORIES_KEY)).isTrue();

        String admin = uniqueEmail();
        createVerifiedUser(admin, "ADMIN");
        mockMvc.perform(post("/api/v1/admin/categories").header("Authorization", login(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Garden"}
                                """))
                .andExpect(status().isCreated());

        assertThat(redis.hasKey(CATEGORIES_KEY)).isFalse();
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(jsonPath("$[*].name", hasItem("Garden")));
    }

    @Test
    void login_isLimitedPerEmail_rightOrWrongPassword() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "CUSTOMER");

        for (int attempt = 1; attempt <= 10; attempt++) {
            mockMvc.perform(loginRequest(email, "wrong-password")).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginRequest(email, PASSWORD))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("TOO_MANY_ATTEMPTS"))
                .andExpect(jsonPath("$.params.minutes").value(15));

        String other = uniqueEmail();
        createVerifiedUser(other, "CUSTOMER");
        mockMvc.perform(loginRequest(other, PASSWORD)).andExpect(status().isOk());

        assertThat(redis.keys("minibasket:ratelimit:login:*")).noneMatch(key -> key.contains("@"));
    }

    @Test
    void passwordResetLinks_areLimitedPerEmail_evenForUnknownAddresses() throws Exception {
        String email = uniqueEmail();
        for (int attempt = 1; attempt <= 3; attempt++) {
            mockMvc.perform(forgotPassword(email)).andExpect(status().isNoContent());
        }
        mockMvc.perform(forgotPassword(email))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("TOO_MANY_ATTEMPTS"));
    }

    private static RequestBuilder loginRequest(String email, String password) {
        return post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
    }

    private static RequestBuilder forgotPassword(String email) {
        return post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s"}
                        """.formatted(email));
    }
}
