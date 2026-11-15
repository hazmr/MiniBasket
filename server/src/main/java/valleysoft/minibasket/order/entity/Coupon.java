package valleysoft.minibasket.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    private Instant expiresAt;

    public Coupon(String code, DiscountType discountType, BigDecimal discountValue, Instant expiresAt) {
        update(code, discountType, discountValue, expiresAt);
    }

    public void update(String code, DiscountType discountType, BigDecimal discountValue, Instant expiresAt) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public BigDecimal discountFor(BigDecimal subtotal) {
        BigDecimal discount = switch (discountType) {
            case PERCENTAGE -> subtotal.multiply(discountValue).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            case FIXED -> discountValue;
        };
        return discount.min(subtotal);
    }
}
