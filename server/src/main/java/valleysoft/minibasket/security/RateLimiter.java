package valleysoft.minibasket.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class RateLimiter {

    private static final DefaultRedisScript<Long> COUNT_ATTEMPT = new DefaultRedisScript<>("""
            local count = redis.call('GET', KEYS[1])
            if not count then
                redis.call('SET', KEYS[1], '1', 'PX', ARGV[2])
                return 0
            end
            if tonumber(count) >= tonumber(ARGV[1]) then
                return redis.call('PTTL', KEYS[1])
            end
            redis.call('INCR', KEYS[1])
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    public void check(String action, String subject, RateLimitProperties.Limit limit) {
        Long millisLeft = redis.execute(COUNT_ATTEMPT, List.of(keyFor(action, subject)),
                Integer.toString(limit.maxAttempts()), Long.toString(limit.window().toMillis()));
        if (millisLeft == null) {
            throw new IllegalStateException("Redis returned no rate-limit result");
        }
        if (millisLeft > 0) {
            long minutes = Math.ceilDiv(millisLeft, 60_000L);
            throw new DomainException(ErrorCode.TOO_MANY_ATTEMPTS, Map.of("minutes", minutes));
        }
    }

    private static String keyFor(String action, String subject) {
        return "minibasket:ratelimit:" + action + ":" + sha256(subject);
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every Java runtime has SHA-256", e);
        }
    }
}
