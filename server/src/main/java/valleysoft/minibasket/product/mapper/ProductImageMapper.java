package valleysoft.minibasket.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.product.dto.ImageUploadResponse;
import valleysoft.minibasket.product.dto.ProductImageResponse;
import valleysoft.minibasket.product.entity.ProductImage;
import valleysoft.minibasket.storage.PresignedUpload;

@Mapper(config = MapStructConfig.class, uses = ProductImageUrls.class)
public interface ProductImageMapper {

    @Mapping(target = "imageUrl", source = "image")
    ProductImageResponse toResponse(ProductImage image);

    ImageUploadResponse toResponse(PresignedUpload upload);
}
