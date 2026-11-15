package valleysoft.minibasket.order.controller;

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
import valleysoft.minibasket.order.dto.ShippingMethodRequest;
import valleysoft.minibasket.order.dto.ShippingMethodResponse;
import valleysoft.minibasket.order.service.ShippingMethodService;

@RestController
@RequestMapping("/api/v1/admin/shipping-methods")
@RequiredArgsConstructor
public class AdminShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShippingMethodResponse createShippingMethod(@Valid @RequestBody ShippingMethodRequest request) {
        return shippingMethodService.createShippingMethod(request);
    }

    @PutMapping("/{id}")
    public ShippingMethodResponse updateShippingMethod(@PathVariable Long id,
            @Valid @RequestBody ShippingMethodRequest request) {
        return shippingMethodService.updateShippingMethod(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShippingMethod(@PathVariable Long id) {
        shippingMethodService.deleteShippingMethod(id);
    }
}
