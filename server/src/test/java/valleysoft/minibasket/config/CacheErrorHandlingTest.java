package valleysoft.minibasket.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;

class CacheErrorHandlingTest {

    private final CacheConfig.FallBackToDatabase handler = new CacheConfig.FallBackToDatabase();
    private final ConcurrentMapCache cache = new ConcurrentMapCache("categories");

    @Test
    void redisDown_onRead_fallsBackToTheDatabase() {
        assertThatCode(() -> handler.handleCacheGetError(new RedisConnectionFailureException("refused"), cache, "all"))
                .doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheGetError(new QueryTimeoutException("slow"), cache, "all"))
                .doesNotThrowAnyException();
    }

    @Test
    void aValueThatCantBeRead_isABug_andStillFails() {
        assertThatThrownBy(() -> handler.handleCacheGetError(new SerializationException("bad json"), cache, "all"))
                .isInstanceOf(SerializationException.class);
    }
}
