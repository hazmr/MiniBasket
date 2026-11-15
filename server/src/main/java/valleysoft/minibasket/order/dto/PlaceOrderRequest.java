package valleysoft.minibasket.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotNull Long addressId,
        @NotNull Long shippingMethodId,
        @Size(max = 50) String couponCode) {
}
