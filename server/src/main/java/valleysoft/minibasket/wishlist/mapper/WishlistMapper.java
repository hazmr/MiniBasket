package valleysoft.minibasket.wishlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.wishlist.dto.WishlistItemResponse;
import valleysoft.minibasket.wishlist.entity.WishlistItem;

@Mapper(config = MapStructConfig.class)
public interface WishlistMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "product.effectivePrice")
    @Mapping(target = "inStock", source = "product.inStock")
    @Mapping(target = "addedAt", source = "createdAt")
    WishlistItemResponse toResponse(WishlistItem item);
}
