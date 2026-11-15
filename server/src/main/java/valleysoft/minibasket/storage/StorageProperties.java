package valleysoft.minibasket.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.storage")
public record StorageProperties(
        @NotBlank String endpoint,
        @NotBlank String region,
        @NotBlank String bucket,
        @NotBlank String accessKeyId,
        @NotBlank String secretAccessKey,
        @NotBlank String publicUrl,
        @NotNull Duration uploadUrlExpiry,
        @NotNull DataSize maxUploadSize) {
}
