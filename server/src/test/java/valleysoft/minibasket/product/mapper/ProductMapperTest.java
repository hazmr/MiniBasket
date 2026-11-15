package valleysoft.minibasket.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.brand.mapper.BrandMapperImpl;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.category.mapper.CategoryMapperImpl;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.dto.ProductResponse;
import valleysoft.minibasket.product.dto.ProductSummaryResponse;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.entity.ProductImage;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock private ProductImageUrls imageUrls;

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapperImpl(new CategoryMapperImpl(), new BrandMapperImpl(),
                new ProductImageMapperImpl(imageUrls), imageUrls);
    }

    @Test
    void toSummary_withSalePriceAndImages_usesFirstImageAndBrandName() {
        Product product = product("19.99", 3, new Brand("Lumo"));
        product.addImage("products/1/front.png");
        product.addImage("products/1/back.png");
        when(imageUrls.urlOf(any(ProductImage.class)))
                .thenAnswer(call -> "https://cdn.test/" + call.<ProductImage>getArgument(0).getStorageKey());

        ProductSummaryResponse summary = mapper.toSummary(product);

        assertThat(summary.imageUrl()).isEqualTo("https://cdn.test/products/1/front.png");
        assertThat(summary.brandName()).isEqualTo("Lumo");
        assertThat(summary.categoryName()).isEqualTo("Lamps");
        assertThat(summary.inStock()).isTrue();
    }

    @Test
    void toSummary_withoutBrandImagesOrStock_leavesThemEmpty() {
        ProductSummaryResponse summary = mapper.toSummary(product(null, 0, null));

        assertThat(summary.brandName()).isNull();
        assertThat(summary.imageUrl()).isNull();
        assertThat(summary.inStock()).isFalse();
    }

    @Test
    void toResponse_withoutBrand_mapsNestedCategoryAndNoBrand() {
        ProductResponse response = mapper.toResponse(product(null, 5, null));

        assertThat(response.category().name()).isEqualTo("Lamps");
        assertThat(response.category().parentId()).isNull();
        assertThat(response.brand()).isNull();
        assertThat(response.images()).isEmpty();
    }

    private static Product product(String discountPrice, int stock, Brand brand) {
        BigDecimal sale = discountPrice == null ? null : new BigDecimal(discountPrice);
        var request = new ProductRequest("LMP-1", "Desk lamp", new BigDecimal("24.99"), sale, stock, 1L, null, null, null);
        return Product.create(request, new Category("Lamps", null), brand);
    }
}
