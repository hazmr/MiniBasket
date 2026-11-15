package valleysoft.minibasket.brand.mapper;

import org.mapstruct.Mapper;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.common.mapping.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface BrandMapper {

    BrandResponse toResponse(Brand brand);
}
