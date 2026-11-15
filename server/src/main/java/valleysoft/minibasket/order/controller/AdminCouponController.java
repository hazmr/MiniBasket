package valleysoft.minibasket.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.order.dto.CouponRequest;
import valleysoft.minibasket.order.dto.CouponResponse;
import valleysoft.minibasket.order.service.CouponService;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public PageResponse<CouponResponse> listCoupons(Pageable pageable) {
        return couponService.listCoupons(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@Valid @RequestBody CouponRequest request) {
        return couponService.createCoupon(request);
    }

    @PutMapping("/{id}")
    public CouponResponse updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return couponService.updateCoupon(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
    }
}
