package valleysoft.minibasket.brand.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.brand.dto.BrandResponse;
import valleysoft.minibasket.brand.service.BrandService;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public List<BrandResponse> listBrands() {
        return brandService.listBrands();
    }
}
