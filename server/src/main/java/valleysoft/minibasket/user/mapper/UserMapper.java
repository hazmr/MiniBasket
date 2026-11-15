package valleysoft.minibasket.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.user.dto.UserResponse;
import valleysoft.minibasket.user.entity.User;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "permissions", source = "role.permissions")
    UserResponse toResponse(User user);
}
