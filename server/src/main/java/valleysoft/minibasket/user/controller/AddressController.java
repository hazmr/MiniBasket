package valleysoft.minibasket.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.security.UserPrincipal;
import valleysoft.minibasket.user.dto.AddressRequest;
import valleysoft.minibasket.user.dto.AddressResponse;
import valleysoft.minibasket.user.service.AddressService;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<AddressResponse> listAddresses(@AuthenticationPrincipal UserPrincipal principal) {
        return addressService.listAddresses(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse addAddress(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        return addressService.addAddress(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public AddressResponse updateAddress(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return addressService.updateAddress(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        addressService.deleteAddress(principal.getId(), id);
    }
}
