package valleysoft.minibasket.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.category.dto.CategoryResponse;

public record ProductResponse(
        Long id, String sku, String name, BigDecimal price, BigDecimal discountPrice, int stockQuantity,
        Map<String, Object> attributes, CategoryResponse category, BrandResponse brand,
        List<ProductImageResponse> images, Long version) {
}
