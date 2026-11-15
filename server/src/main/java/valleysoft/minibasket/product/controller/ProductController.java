package valleysoft.minibasket.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.product.dto.ProductResponse;
import valleysoft.minibasket.product.dto.ProductSummaryResponse;
import valleysoft.minibasket.product.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public PageResponse<ProductSummaryResponse> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.searchProducts(categoryId, brandId, name, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }
}
