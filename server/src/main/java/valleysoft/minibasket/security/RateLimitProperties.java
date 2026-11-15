package valleysoft.minibasket.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(@Valid @NotNull Limit login, @Valid @NotNull Limit passwordReset) {

    public record Limit(@Positive int maxAttempts, @NotNull Duration window) {
    }
}
