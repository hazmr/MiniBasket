package valleysoft.minibasket.product.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import valleysoft.minibasket.product.entity.ProductImage;
import valleysoft.minibasket.storage.StorageService;

@Component
@RequiredArgsConstructor
public class ProductImageUrls {

    private final StorageService storageService;

    public String urlOf(ProductImage image) {
        return storageService.publicUrl(image.getStorageKey());
    }
}
