package valleysoft.minibasket.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.product.dto.AddProductImageRequest;
import valleysoft.minibasket.product.dto.ImageUploadRequest;
import valleysoft.minibasket.product.dto.ImageUploadResponse;
import valleysoft.minibasket.product.dto.ProductImageResponse;
import valleysoft.minibasket.product.service.ProductImageService;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@RequiredArgsConstructor
public class AdminProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/upload-url")
    public ImageUploadResponse createImageUpload(@PathVariable Long productId, @Valid @RequestBody ImageUploadRequest request) {
        return productImageService.createImageUpload(productId, request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse addImage(@PathVariable Long productId, @Valid @RequestBody AddProductImageRequest request) {
        return productImageService.addImage(productId, request);
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.removeImage(productId, imageId);
    }
}
