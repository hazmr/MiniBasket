package valleysoft.minibasket.user.mapper;

import org.mapstruct.Mapper;
import valleysoft.minibasket.common.mapping.MapStructConfig;
import valleysoft.minibasket.user.dto.AddressResponse;
import valleysoft.minibasket.user.entity.Address;

@Mapper(config = MapStructConfig.class)
public interface AddressMapper {

    AddressResponse toResponse(Address address);
}
