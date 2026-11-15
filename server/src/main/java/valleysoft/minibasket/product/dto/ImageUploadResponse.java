package valleysoft.minibasket.product.dto;

import java.time.Instant;

public record ImageUploadResponse(String key, String uploadUrl, Instant expiresAt) {
}
