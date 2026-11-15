package valleysoft.minibasket.order.mapper;

import org.mapstruct.Mapper;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.order.dto.ShippingMethodResponse;
import valleysoft.minibasket.order.entity.ShippingMethod;

@Mapper(config = MapStructConfig.class)
public interface ShippingMethodMapper {

    ShippingMethodResponse toResponse(ShippingMethod shippingMethod);
}
