package valleysoft.minibasket.wishlist.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.user.repository.UserRepository;
import valleysoft.minibasket.wishlist.dto.AddWishlistItemRequest;
import valleysoft.minibasket.wishlist.dto.WishlistItemResponse;
import valleysoft.minibasket.wishlist.entity.WishlistItem;
import valleysoft.minibasket.wishlist.mapper.WishlistMapper;
import valleysoft.minibasket.wishlist.repository.WishlistItemRepository;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistMapper wishlistMapper;

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getWishlist(Long userId) {
        return wishlistItemRepository.findAllWithProductByUserId(userId).stream()
                .map(wishlistMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<WishlistItemResponse> addItem(Long userId, AddWishlistItemRequest request) {
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND, Map.of("id", request.productId())));
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, product.getId())) {
            wishlistItemRepository.save(new WishlistItem(userRepository.getReferenceById(userId), product));
        }
        return getWishlist(userId);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        if (wishlistItemRepository.deleteByUserIdAndProductId(userId, productId) == 0) {
            throw new DomainException(ErrorCode.WISHLIST_ITEM_NOT_FOUND, Map.of("id", productId));
        }
    }
}
