package valleysoft.minibasket.order.mapper;

import org.mapstruct.Mapper;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.order.dto.CouponResponse;
import valleysoft.minibasket.order.entity.Coupon;

@Mapper(config = MapStructConfig.class)
public interface CouponMapper {

    CouponResponse toResponse(Coupon coupon);
}
