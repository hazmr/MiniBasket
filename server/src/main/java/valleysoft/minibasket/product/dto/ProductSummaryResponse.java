package valleysoft.minibasket.product.dto;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        Long id, String sku, String name, BigDecimal price, BigDecimal discountPrice,
        boolean inStock, String categoryName, String brandName, String imageUrl) {
}
