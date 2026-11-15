package valleysoft.minibasket.order.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.cache.CacheNames;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.order.dto.ShippingMethodRequest;
import valleysoft.minibasket.order.dto.ShippingMethodResponse;
import valleysoft.minibasket.order.entity.ShippingMethod;
import valleysoft.minibasket.order.mapper.ShippingMethodMapper;
import valleysoft.minibasket.order.repository.ShippingMethodRepository;

@Service
@RequiredArgsConstructor
public class ShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingMethodMapper shippingMethodMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.SHIPPING_METHODS, key = CacheNames.ALL)
    public List<ShippingMethodResponse> listShippingMethods() {
        return shippingMethodRepository.findAll(Sort.by("fee")).stream().map(shippingMethodMapper::toResponse).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SHIPPING_METHODS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    public ShippingMethodResponse createShippingMethod(ShippingMethodRequest request) {
        ShippingMethod shippingMethod = new ShippingMethod(request.name().trim(), request.fee());
        return shippingMethodMapper.toResponse(shippingMethodRepository.save(shippingMethod));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SHIPPING_METHODS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    public ShippingMethodResponse updateShippingMethod(Long id, ShippingMethodRequest request) {
        ShippingMethod shippingMethod = findShippingMethod(id);
        shippingMethod.update(request.name().trim(), request.fee());
        return shippingMethodMapper.toResponse(shippingMethod);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.SHIPPING_METHODS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    public void deleteShippingMethod(Long id) {
        shippingMethodRepository.delete(findShippingMethod(id));
        shippingMethodRepository.flush();
    }

    private ShippingMethod findShippingMethod(Long id) {
        return shippingMethodRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.SHIPPING_METHOD_NOT_FOUND, Map.of("id", id)));
    }
}
