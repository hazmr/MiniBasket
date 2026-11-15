package valleysoft.minibasket.product.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.brand.repository.BrandRepository;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.category.repository.CategoryRepository;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.dto.ProductResponse;
import valleysoft.minibasket.product.dto.ProductSummaryResponse;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.entity.ProductImage;
import valleysoft.minibasket.product.mapper.ProductMapper;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.storage.FilesNoLongerUsedEvent;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "price", "createdAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ApplicationEventPublisher events;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> searchProducts(Long categoryId, Long brandId, String name, Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new DomainException(ErrorCode.INVALID_SORT, Map.of("property", order.getProperty()));
            }
        });
        String nameFilter = name == null ? "" : name.trim();
        return PageResponse.from(productRepository.search(categoryId, brandId, nameFilter, pageable)
                .map(productMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Transactional
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ProductResponse createProduct(ProductRequest request) {
        validateDiscount(request);
        if (productRepository.existsBySku(request.sku().trim())) {
            throw new DomainException(ErrorCode.SKU_EXISTS, Map.of("sku", request.sku().trim()));
        }
        Product product = Product.create(request, findCategory(request.categoryId()), findBrand(request.brandId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateDiscount(request);
        Product product = findProduct(id);
        if (!product.getVersion().equals(request.version())) {
            throw new DomainException(ErrorCode.PRODUCT_CHANGED);
        }
        if (productRepository.existsBySkuAndIdNot(request.sku().trim(), id)) {
            throw new DomainException(ErrorCode.SKU_EXISTS, Map.of("sku", request.sku().trim()));
        }
        product.update(request, findCategory(request.categoryId()), findBrand(request.brandId()));
        return productMapper.toResponse(product);
    }

    @Transactional
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        List<String> storedKeys = product.getImages().stream().map(ProductImage::getStorageKey).toList();
        productRepository.delete(product);
        productRepository.flush();
        events.publishEvent(new FilesNoLongerUsedEvent(storedKeys));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND, Map.of("id", id)));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND, Map.of("id", categoryId)));
    }

    private Brand findBrand(Long brandId) {
        if (brandId == null) {
            return null;
        }
        return brandRepository.findById(brandId).orElseThrow(() -> new DomainException(ErrorCode.BRAND_NOT_FOUND, Map.of("id", brandId)));
    }

    private static void validateDiscount(ProductRequest request) {
        if (request.discountPrice() != null && request.discountPrice().compareTo(request.price()) >= 0) {
            throw new DomainException(ErrorCode.INVALID_DISCOUNT);
        }
    }
}
