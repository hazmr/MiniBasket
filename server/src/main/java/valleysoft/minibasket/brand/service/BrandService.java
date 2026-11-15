package valleysoft.minibasket.brand.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.brand.dto.BrandRequest;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.brand.entity.Brand;
import valleysoft.minibasket.brand.mapper.BrandMapper;
import valleysoft.minibasket.brand.repository.BrandRepository;
import valleysoft.minibasket.common.cache.CacheNames;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
    public List<BrandResponse> listBrands() {
        return brandRepository.findAll(Sort.by("name")).stream().map(brandMapper::toResponse).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public BrandResponse createBrand(BrandRequest request) {
        String name = request.name().trim();
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new DomainException(ErrorCode.BRAND_EXISTS, Map.of("name", name));
        }
        return brandMapper.toResponse(brandRepository.save(new Brand(name)));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = getBrand(id);
        String name = request.name().trim();
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DomainException(ErrorCode.BRAND_EXISTS, Map.of("name", name));
        }
        brand.rename(name);
        return brandMapper.toResponse(brand);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.BRANDS, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public void deleteBrand(Long id) {
        brandRepository.delete(getBrand(id));
        brandRepository.flush();
    }

    private Brand getBrand(Long id) {
        return brandRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.BRAND_NOT_FOUND, Map.of("id", id)));
    }
}
