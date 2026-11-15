package valleysoft.minibasket.order.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.order.dto.ShippingMethodResponse;
import valleysoft.minibasket.order.service.ShippingMethodService;

@RestController
@RequestMapping("/api/v1/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @GetMapping
    public List<ShippingMethodResponse> listShippingMethods() {
        return shippingMethodService.listShippingMethods();
    }
}
