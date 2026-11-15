package valleysoft.minibasket.wishlist.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.security.UserPrincipal;
import valleysoft.minibasket.wishlist.dto.AddWishlistItemRequest;
import valleysoft.minibasket.wishlist.dto.WishlistItemResponse;
import valleysoft.minibasket.wishlist.service.WishlistService;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public List<WishlistItemResponse> getWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        return wishlistService.getWishlist(principal.getId());
    }

    @PostMapping("/items")
    public List<WishlistItemResponse> addItem(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddWishlistItemRequest request) {
        return wishlistService.addItem(principal.getId(), request);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId) {
        wishlistService.removeItem(principal.getId(), productId);
    }
}
