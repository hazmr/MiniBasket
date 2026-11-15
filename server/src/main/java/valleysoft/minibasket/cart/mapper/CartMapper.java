package valleysoft.minibasket.cart.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.cart.dto.CartResponse;
import valleysoft.minibasket.cart.entity.CartItem;
import valleysoft.minibasket.common.mapping.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface CartMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitPrice", source = "product.effectivePrice")
    CartResponse.Item toItem(CartItem cartItem);

    List<CartResponse.Item> toItems(List<CartItem> cartItems);

    default CartResponse toResponse(List<CartItem> cartItems) {
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subtotal = cartItems.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(toItems(cartItems), totalQuantity, subtotal);
    }
}
