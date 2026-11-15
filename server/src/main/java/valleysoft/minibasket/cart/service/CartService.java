package valleysoft.minibasket.cart.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.cart.dto.AddCartItemRequest;
import valleysoft.minibasket.cart.dto.CartResponse;
import valleysoft.minibasket.cart.dto.UpdateCartItemRequest;
import valleysoft.minibasket.cart.entity.CartItem;
import valleysoft.minibasket.cart.mapper.CartMapper;
import valleysoft.minibasket.cart.repository.CartItemRepository;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartMapper.toResponse(cartItemRepository.findAllWithProductByUserId(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND, Map.of("id", request.productId())));

        cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .ifPresentOrElse(
                        existing -> {
                            int newQuantity = existing.getQuantity() + request.quantity();
                            checkStock(product, newQuantity);
                            existing.changeQuantity(newQuantity);
                        },
                        () -> {
                            checkStock(product, request.quantity());
                            cartItemRepository.save(
                                    new CartItem(userRepository.getReferenceById(userId), product, request.quantity()));
                        });

        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = findCartItem(userId, cartItemId);
        checkStock(cartItem.getProduct(), request.quantity());
        cartItem.changeQuantity(request.quantity());
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        cartItemRepository.delete(findCartItem(userId, cartItemId));
        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }

    private CartItem findCartItem(Long userId, Long cartItemId) {
        return cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new DomainException(ErrorCode.CART_ITEM_NOT_FOUND, Map.of("id", cartItemId)));
    }

    private static void checkStock(Product product, int wantedQuantity) {
        if (wantedQuantity > product.getStockQuantity()) {
            throw new DomainException(ErrorCode.INSUFFICIENT_STOCK,
                    Map.of("available", product.getStockQuantity(), "productName", product.getName()));
        }
    }
}
