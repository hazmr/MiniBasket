package valleysoft.minibasket.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.category.dto.CategoryResponse;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.common.mapping.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);
}
