package valleysoft.minibasket.category.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.category.dto.CategoryResponse;
import valleysoft.minibasket.category.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> listCategories() {
        return categoryService.listCategories();
    }
}
