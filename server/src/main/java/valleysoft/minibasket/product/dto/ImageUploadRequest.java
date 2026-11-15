package valleysoft.minibasket.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ImageUploadRequest(@NotBlank String contentType, @Positive long size) {
}
