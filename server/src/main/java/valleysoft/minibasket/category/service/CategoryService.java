package valleysoft.minibasket.category.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.category.dto.CategoryRequest;
import valleysoft.minibasket.category.dto.CategoryResponse;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.category.mapper.CategoryMapper;
import valleysoft.minibasket.category.repository.CategoryRepository;
import valleysoft.minibasket.common.cache.CacheNames;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORIES, key = CacheNames.ALL)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CATEGORIES, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = request.parentId() == null ? null : getCategory(request.parentId());
        return categoryMapper.toResponse(categoryRepository.save(new Category(request.name().trim(), parent)));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CATEGORIES, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        Category parent = request.parentId() == null ? null : getCategory(request.parentId());
        if (parent != null && createsCycle(category, parent)) {
            throw new DomainException(ErrorCode.CATEGORY_CYCLE);
        }
        category.update(request.name().trim(), parent);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.CATEGORIES, key = CacheNames.ALL)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public void deleteCategory(Long id) {
        categoryRepository.delete(getCategory(id));
        categoryRepository.flush();
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND, Map.of("id", id)));
    }

    private boolean createsCycle(Category category, Category newParent) {
        for (Category current = newParent; current != null; current = current.getParent()) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
        }
        return false;
    }
}
