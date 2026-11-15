package valleysoft.minibasket.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProductImageRequest(@NotBlank @Size(max = 300) String key) {
}
