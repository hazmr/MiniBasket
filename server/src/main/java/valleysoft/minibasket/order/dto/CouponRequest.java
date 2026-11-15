package valleysoft.minibasket.order.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import valleysoft.minibasket.order.entity.DiscountType;

public record CouponRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Za-z0-9_-]+", message = "may only contain letters, digits, - and _")
        String code,
        @NotNull DiscountType discountType,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal discountValue,
        Instant expiresAt) {
}
