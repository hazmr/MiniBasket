package valleysoft.minibasket.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 200) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal discountPrice,
        @NotNull @PositiveOrZero Integer stockQuantity,
        @NotNull Long categoryId,
        Long brandId,
        Map<String, Object> attributes,
        Long version) {
}
