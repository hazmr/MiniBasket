package valleysoft.minibasket.brand.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.brand.dto.BrandRequest;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.brand.service.BrandService;

@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse createBrand(@Valid @RequestBody BrandRequest request) {
        return brandService.createBrand(request);
    }

    @PutMapping("/{id}")
    public BrandResponse updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return brandService.updateBrand(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
    }
}
