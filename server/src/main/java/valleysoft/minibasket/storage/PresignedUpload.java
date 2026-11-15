package valleysoft.minibasket.storage;

import java.time.Instant;

public record PresignedUpload(String key, String uploadUrl, Instant expiresAt) {
}
