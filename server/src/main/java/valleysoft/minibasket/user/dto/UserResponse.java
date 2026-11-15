package valleysoft.minibasket.user.dto;

import java.time.Instant;
import java.util.Set;
import valleysoft.minibasket.user.entity.Permission;

public record UserResponse(
        Long id, String email, String role, Set<Permission> permissions, boolean emailVerified, Instant createdAt) {
}
