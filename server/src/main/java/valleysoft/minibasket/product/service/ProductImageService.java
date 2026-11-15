package valleysoft.minibasket.product.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.dto.AddProductImageRequest;
import valleysoft.minibasket.product.dto.ImageUploadRequest;
import valleysoft.minibasket.product.dto.ImageUploadResponse;
import valleysoft.minibasket.product.dto.ProductImageResponse;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.entity.ProductImage;
import valleysoft.minibasket.product.mapper.ProductImageMapper;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.storage.FilesNoLongerUsedEvent;
import valleysoft.minibasket.storage.StorageService;
import valleysoft.minibasket.storage.StoredFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher events;
    private final ProductImageMapper productImageMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ImageUploadResponse createImageUpload(Long productId, ImageUploadRequest request) {
        findProduct(productId);
        return productImageMapper.toResponse(
                storageService.presignImageUpload(imageFolder(productId), request.contentType(), request.size()));
    }

    @Transactional
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ProductImageResponse addImage(Long productId, AddProductImageRequest request) {
        Product product = findProduct(productId);
        String key = request.key();
        if (!key.startsWith(imageFolder(productId) + "/")) {
            throw new DomainException(ErrorCode.INVALID_IMAGE_KEY);
        }
        StoredFile file = storageService.find(key)
                .orElseThrow(() -> new DomainException(ErrorCode.IMAGE_NOT_UPLOADED));
        if (file.contentType() == null || !file.contentType().startsWith("image/") || file.size() > storageService.maxUploadBytes()) {
            throw new DomainException(ErrorCode.INVALID_IMAGE);
        }

        ProductImage image = product.addImage(key);
        productRepository.flush();
        return productImageMapper.toResponse(image);
    }

    @Transactional
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public void removeImage(Long productId, Long imageId) {
        Product product = findProduct(productId);
        ProductImage image = product.getImages().stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND, Map.of("id", imageId)));
        product.removeImage(image);
        events.publishEvent(new FilesNoLongerUsedEvent(List.of(image.getStorageKey())));
    }

    private static String imageFolder(Long productId) {
        return "products/" + productId;
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND, Map.of("id", id)));
    }
}
