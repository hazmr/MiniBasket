package valleysoft.minibasket.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import valleysoft.minibasket.cart.dto.AddCartItemRequest;
import valleysoft.minibasket.cart.dto.CartResponse;
import valleysoft.minibasket.cart.dto.UpdateCartItemRequest;
import valleysoft.minibasket.cart.entity.CartItem;
import valleysoft.minibasket.cart.mapper.CartMapper;
import valleysoft.minibasket.cart.mapper.CartMapperImpl;
import valleysoft.minibasket.cart.repository.CartItemRepository;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.user.entity.User;
import valleysoft.minibasket.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final Long USER_ID = 1L;

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Spy private CartMapper cartMapper = new CartMapperImpl();

    @InjectMocks private CartService cartService;

    @Test
    void addItem_whenMoreThanInStock_throwsInsufficientStock() {
        Product kettle = product(5L, "Kettle", "40.00", 2);
        when(productRepository.findById(5L)).thenReturn(Optional.of(kettle));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(USER_ID, new AddCartItemRequest(5L, 3)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_whenProductAlreadyInCart_increasesQuantityInsteadOfAddingALine() {
        Product kettle = product(5L, "Kettle", "40.00", 10);
        CartItem existing = new CartItem(mock(User.class), kettle, 1);
        when(productRepository.findById(5L)).thenReturn(Optional.of(kettle));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 5L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findAllWithProductByUserId(USER_ID)).thenReturn(List.of(existing));

        CartResponse cart = cartService.addItem(USER_ID, new AddCartItemRequest(5L, 2));

        assertThat(existing.getQuantity()).isEqualTo(3);
        assertThat(cart.subtotal()).isEqualByComparingTo("120.00");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateItem_ofAnotherUsersCartItem_throwsNotFound() {
        when(cartItemRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItem(USER_ID, 99L, new UpdateCartItemRequest(1)))
                .isInstanceOf(DomainException.class);
    }

    private static Product product(Long id, String name, String price, int stock) {
        var request = new ProductRequest("SKU-" + id, name, new BigDecimal(price), null, stock, 1L, null, null, null);
        Product product = Product.create(request, new Category("Test", null), null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
