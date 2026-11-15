package valleysoft.minibasket.cart.controller;

import jakarta.validation.Valid;
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
import valleysoft.minibasket.cart.dto.AddCartItemRequest;
import valleysoft.minibasket.cart.dto.CartResponse;
import valleysoft.minibasket.cart.dto.UpdateCartItemRequest;
import valleysoft.minibasket.cart.service.CartService;
import valleysoft.minibasket.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return cartService.getCart(principal.getId());
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(principal.getId(), request);
    }

    @PutMapping("/items/{id}")
    public CartResponse updateItem(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(principal.getId(), id, request);
    }

    @DeleteMapping("/items/{id}")
    public CartResponse removeItem(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return cartService.removeItem(principal.getId(), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
    }
}
