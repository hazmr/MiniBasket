package valleysoft.minibasket.wishlist.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WishlistItemResponse(Long productId, String productName, BigDecimal price, boolean inStock, Instant addedAt) {
}
