package valleysoft.minibasket.config;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.category.dto.CategoryResponse;
import valleysoft.minibasket.common.cache.CacheNames;
import valleysoft.minibasket.order.dto.ShippingMethodResponse;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Duration TTL = Duration.ofMinutes(10);

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, JsonMapper jsonMapper) {
        RedisCacheWriter writer = RedisCacheWriter.create(connectionFactory,
                settings -> settings.immediateWrites().batchStrategy(BatchStrategies.scan(1000)));

        return RedisCacheManager.builder(writer)
                .withCacheConfiguration(CacheNames.CATEGORIES, listOf(CategoryResponse.class, jsonMapper))
                .withCacheConfiguration(CacheNames.BRANDS, listOf(BrandResponse.class, jsonMapper))
                .withCacheConfiguration(CacheNames.SHIPPING_METHODS, listOf(ShippingMethodResponse.class, jsonMapper))
                .disableCreateOnMissingCache()
                .transactionAware()
                .build();
    }

    private static RedisCacheConfiguration listOf(Class<?> elementType, JsonMapper jsonMapper) {
        JavaType listType = jsonMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> "minibasket:" + cacheName + ":")
                .entryTtl((key, value) -> TTL.plusSeconds(ThreadLocalRandom.current().nextLong(60)))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(jsonMapper, listType)));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new FallBackToDatabase();
    }

    static class FallBackToDatabase implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            rethrowUnlessRedisIsDown(exception);
            log.warn("Redis unavailable, reading {} from the database: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            rethrowUnlessRedisIsDown(exception);
            log.warn("Redis unavailable, not caching {}: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            rethrowUnlessRedisIsDown(exception);
            log.error("Redis unavailable, could not evict {}; it may be stale for up to {}", cache.getName(), TTL);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            rethrowUnlessRedisIsDown(exception);
            log.error("Redis unavailable, could not clear {}; it may be stale for up to {}", cache.getName(), TTL);
        }

        private static void rethrowUnlessRedisIsDown(RuntimeException exception) {
            if (!(exception instanceof DataAccessResourceFailureException || exception instanceof QueryTimeoutException)) {
                throw exception;
            }
        }
    }
}
