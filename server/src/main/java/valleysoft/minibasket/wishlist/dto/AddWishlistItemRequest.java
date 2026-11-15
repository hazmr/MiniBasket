package valleysoft.minibasket.wishlist.dto;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(@NotNull Long productId) {
}
