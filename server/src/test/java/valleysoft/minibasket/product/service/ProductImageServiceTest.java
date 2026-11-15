package valleysoft.minibasket.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.dto.AddProductImageRequest;
import valleysoft.minibasket.product.dto.ProductImageResponse;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.entity.ProductImage;
import valleysoft.minibasket.product.mapper.ProductImageMapperImpl;
import valleysoft.minibasket.product.mapper.ProductImageUrls;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.storage.FilesNoLongerUsedEvent;
import valleysoft.minibasket.storage.StorageService;
import valleysoft.minibasket.storage.StoredFile;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    private static final Long PRODUCT_ID = 7L;
    private static final String KEY = "products/7/abc.png";

    @Mock private ProductRepository productRepository;
    @Mock private StorageService storageService;
    @Mock private ApplicationEventPublisher events;
    @Mock private ProductImageUrls imageUrls;

    private ProductImageService productImageService;

    @BeforeEach
    void createService() {
        productImageService = new ProductImageService(productRepository, storageService, events,
                new ProductImageMapperImpl(imageUrls));
    }

    private Product product;

    @BeforeEach
    void setUp() {
        var request = new ProductRequest("MUG-1", "Mug", new BigDecimal("12.50"), null, 5, 1L, null, null, null);
        product = Product.create(request, new Category("Kitchen", null), null);
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    }

    @Test
    void addImage_withKeyOfAnotherProduct_isRejected() {
        assertThatThrownBy(() -> productImageService.addImage(PRODUCT_ID, new AddProductImageRequest("products/8/abc.png")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_IMAGE_KEY);
        verify(storageService, never()).find(any());
    }

    @Test
    void addImage_beforeTheFileIsUploaded_isRejected() {
        when(storageService.find(KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.addImage(PRODUCT_ID, new AddProductImageRequest(KEY)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_UPLOADED);
        assertThat(product.getImages()).isEmpty();
    }

    @Test
    void addImage_withUploadedImage_storesPublicUrlAndKey() {
        when(storageService.find(KEY)).thenReturn(Optional.of(new StoredFile(KEY, "image/png", 1024)));
        when(storageService.maxUploadBytes()).thenReturn(5L * 1024 * 1024);
        when(imageUrls.urlOf(any())).thenReturn("https://pub-test.r2.dev/" + KEY);

        ProductImageResponse response = productImageService.addImage(PRODUCT_ID, new AddProductImageRequest(KEY));

        assertThat(response.imageUrl()).isEqualTo("https://pub-test.r2.dev/" + KEY);
        assertThat(product.getImages()).extracting(ProductImage::getStorageKey).containsExactly(KEY);
    }

    @Test
    void addImage_whenTheUploadIsNotAnImage_isRejected() {
        when(storageService.find(KEY)).thenReturn(Optional.of(new StoredFile(KEY, "text/html", 1024)));

        assertThatThrownBy(() -> productImageService.addImage(PRODUCT_ID, new AddProductImageRequest(KEY)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_IMAGE);
    }

    @Test
    void removeImage_schedulesTheFileForDeletion() {
        ProductImage image = product.addImage(KEY);
        ReflectionTestUtils.setField(image, "id", 3L);

        productImageService.removeImage(PRODUCT_ID, 3L);

        assertThat(product.getImages()).isEmpty();
        verify(events).publishEvent(new FilesNoLongerUsedEvent(List.of(KEY)));
    }
}
