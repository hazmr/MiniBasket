package valleysoft.minibasket.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import valleysoft.minibasket.order.entity.DiscountType;

public record CouponResponse(Long id, String code, DiscountType discountType, BigDecimal discountValue,
        Instant expiresAt, boolean expired) {
}
