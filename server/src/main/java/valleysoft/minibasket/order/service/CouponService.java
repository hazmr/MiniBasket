package valleysoft.minibasket.order.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.order.dto.CouponRequest;
import valleysoft.minibasket.order.dto.CouponResponse;
import valleysoft.minibasket.order.entity.Coupon;
import valleysoft.minibasket.order.entity.DiscountType;
import valleysoft.minibasket.order.mapper.CouponMapper;
import valleysoft.minibasket.order.repository.CouponRepository;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    public PageResponse<CouponResponse> listCoupons(Pageable pageable) {
        return PageResponse.from(couponRepository.findAll(pageable).map(couponMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    public CouponResponse createCoupon(CouponRequest request) {
        validate(request);
        String code = normalizeCode(request.code());
        if (couponRepository.existsByCode(code)) {
            throw new DomainException(ErrorCode.COUPON_EXISTS, Map.of("code", code));
        }
        Coupon coupon = new Coupon(code, request.discountType(), request.discountValue(), request.expiresAt());
        return couponMapper.toResponse(couponRepository.save(coupon));
    }

    @Transactional
    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        validate(request);
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.COUPON_NOT_FOUND, Map.of("id", id)));
        String code = normalizeCode(request.code());
        if (couponRepository.existsByCodeAndIdNot(code, id)) {
            throw new DomainException(ErrorCode.COUPON_EXISTS, Map.of("code", code));
        }
        coupon.update(code, request.discountType(), request.discountValue(), request.expiresAt());
        return couponMapper.toResponse(coupon);
    }

    @Transactional
    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    public void deleteCoupon(Long id) {
        couponRepository.delete(couponRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.COUPON_NOT_FOUND, Map.of("id", id))));
        couponRepository.flush();
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> findUsableCoupon(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        Coupon coupon = couponRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_COUPON, Map.of("code", code)));
        if (coupon.isExpired()) {
            throw new DomainException(ErrorCode.COUPON_EXPIRED, Map.of("code", coupon.getCode()));
        }
        return Optional.of(coupon);
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void validate(CouponRequest request) {
        if (request.discountType() == DiscountType.PERCENTAGE && request.discountValue().compareTo(MAX_PERCENTAGE) > 0) {
            throw new DomainException(ErrorCode.PERCENTAGE_TOO_HIGH);
        }
    }
}
