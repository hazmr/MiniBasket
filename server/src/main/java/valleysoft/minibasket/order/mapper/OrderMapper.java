package valleysoft.minibasket.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.order.dto.OrderResponse;
import valleysoft.minibasket.order.dto.OrderSummaryResponse;
import valleysoft.minibasket.order.entity.Order;
import valleysoft.minibasket.order.entity.OrderItem;

@Mapper(config = MapStructConfig.class)
public interface OrderMapper {

    @Mapping(target = "couponCode", source = "coupon.code")
    @Mapping(target = "shippingMethod", source = "shippingMethod.name")
    OrderResponse toResponse(Order order);

    OrderSummaryResponse toSummary(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderResponse.Item toItem(OrderItem item);
}
