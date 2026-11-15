package valleysoft.minibasket.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(@NotNull Long productId, @NotNull @Positive @Max(100) Integer quantity) {
}
