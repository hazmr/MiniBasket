package valleysoft.minibasket.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import valleysoft.minibasket.brand.mapper.BrandMapperImpl;
import valleysoft.minibasket.brand.repository.BrandRepository;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.category.mapper.CategoryMapperImpl;
import valleysoft.minibasket.category.repository.CategoryRepository;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.mapper.ProductImageMapperImpl;
import valleysoft.minibasket.product.mapper.ProductImageUrls;
import valleysoft.minibasket.product.mapper.ProductMapper;
import valleysoft.minibasket.product.mapper.ProductMapperImpl;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.storage.FilesNoLongerUsedEvent;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final Long PRODUCT_ID = 1L;

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private ApplicationEventPublisher events;
    @Mock private ProductImageUrls imageUrls;

    private ProductService productService;

    @BeforeEach
    void createService() {
        ProductMapper productMapper = new ProductMapperImpl(new CategoryMapperImpl(), new BrandMapperImpl(),
                new ProductImageMapperImpl(imageUrls), imageUrls);
        productService = new ProductService(productRepository, categoryRepository, brandRepository, events, productMapper);
    }

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("Phones", null);
        ReflectionTestUtils.setField(category, "id", 2L);
        product = Product.create(request("Nova X1", 10, null), category, null);
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        ReflectionTestUtils.setField(product, "version", 5L);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    }

    @Test
    void updateProduct_fromAnOlderCopy_isRefusedAndChangesNothing() {
        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request("Nova X1 Pro", 10, 4L)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_CHANGED);
        assertThat(product.getName()).isEqualTo("Nova X1");
    }

    @Test
    void updateProduct_withoutAVersion_isRefused() {
        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request("Nova X1 Pro", 10, null)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void updateProduct_fromTheCurrentVersion_saves() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        productService.updateProduct(PRODUCT_ID, request("Nova X1 Pro", 8, 5L));

        assertThat(product.getName()).isEqualTo("Nova X1 Pro");
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

@Test
    void deleteProduct_schedulesItsStoredFilesForDeletion() {
        product.addImage("products/7/front.png");

        productService.deleteProduct(PRODUCT_ID);

        verify(productRepository).delete(product);
        verify(events).publishEvent(new FilesNoLongerUsedEvent(List.of("products/7/front.png")));
    }

        private static ProductRequest request(String name, int stock, Long version) {
        return new ProductRequest("PHN-1", name, new BigDecimal("699.00"), null, stock, 2L, null, null, version);
    }
}
