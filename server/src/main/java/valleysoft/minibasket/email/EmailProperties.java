package valleysoft.minibasket.email;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.email")
public record EmailProperties(boolean enabled, @NotBlank String from, @NotBlank String frontendUrl) {
}
