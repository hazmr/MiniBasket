package valleysoft.minibasket.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.brand.mapper.BrandMapper;
import valleysoft.minibasket.category.mapper.CategoryMapper;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.product.dto.ProductResponse;
import valleysoft.minibasket.product.dto.ProductSummaryResponse;
import valleysoft.minibasket.product.entity.Product;

@Mapper(config = MapStructConfig.class,
        uses = {CategoryMapper.class, BrandMapper.class, ProductImageMapper.class, ProductImageUrls.class})
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "imageUrl", source = "coverImage")
    ProductSummaryResponse toSummary(Product product);
}
