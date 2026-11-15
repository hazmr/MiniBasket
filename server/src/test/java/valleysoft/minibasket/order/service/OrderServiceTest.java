package valleysoft.minibasket.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import valleysoft.minibasket.cart.entity.CartItem;
import valleysoft.minibasket.cart.mapper.CartMapperImpl;
import valleysoft.minibasket.cart.repository.CartItemRepository;
import valleysoft.minibasket.cart.service.CartService;
import valleysoft.minibasket.category.entity.Category;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.order.dto.OrderResponse;
import valleysoft.minibasket.order.dto.PlaceOrderRequest;
import valleysoft.minibasket.order.entity.Coupon;
import valleysoft.minibasket.order.entity.DiscountType;
import valleysoft.minibasket.order.entity.Order;
import valleysoft.minibasket.order.entity.OrderStatus;
import valleysoft.minibasket.order.entity.PaymentStatus;
import valleysoft.minibasket.order.entity.ShippingMethod;
import valleysoft.minibasket.order.mapper.CouponMapperImpl;
import valleysoft.minibasket.order.mapper.OrderMapperImpl;
import valleysoft.minibasket.order.repository.CouponRepository;
import valleysoft.minibasket.order.repository.OrderRepository;
import valleysoft.minibasket.order.repository.ShippingMethodRepository;
import valleysoft.minibasket.product.dto.ProductRequest;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.repository.ProductRepository;
import valleysoft.minibasket.product.service.StockService;
import valleysoft.minibasket.user.entity.Address;
import valleysoft.minibasket.user.entity.User;
import valleysoft.minibasket.user.repository.AddressRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;
    private static final Long SHIPPING_ID = 20L;

    @Mock private OrderRepository orderRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ShippingMethodRepository shippingMethodRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private UserRepository userRepository;

    private OrderService orderService;

    private User user;
    private Address address;
    private ShippingMethod standardShipping;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartItemRepository,
                new CartService(cartItemRepository, productRepository, userRepository, new CartMapperImpl()),
                addressRepository, shippingMethodRepository,
                new CouponService(couponRepository, new CouponMapperImpl()), new StockService(productRepository),
                new OrderNumberGenerator(), userRepository, new OrderMapperImpl());
        user = new User("sara@example.com", "hash", null);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        address = new Address(user, "12 Olive St", "Amman");
        standardShipping = new ShippingMethod("Standard", new BigDecimal("5.00"));
    }

    @Test
    void placeOrder_withCouponAndShipping_calculatesTotalsTakesStockAndEmptiesCart() {
        Product phone = product(1L, "Phone", "100.00", "80.00", 10);
        Product kettle = product(2L, "Kettle", "50.00", null, 10);
        givenCart(new CartItem(user, phone, 2), new CartItem(user, kettle, 1));
        givenAddressAndShipping();
        when(couponRepository.findByCode("WELCOME10"))
                .thenReturn(Optional.of(new Coupon("WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10"), null)));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(productRepository.decreaseStock(anyLong(), anyInt())).thenReturn(1);

        OrderResponse order = orderService.placeOrder(USER_ID,
                new PlaceOrderRequest(ADDRESS_ID, SHIPPING_ID, " welcome10 "));

        assertThat(order.subtotal()).isEqualByComparingTo("210.00");
        assertThat(order.discountAmount()).isEqualByComparingTo("21.00");
        assertThat(order.shippingFee()).isEqualByComparingTo("5.00");
        assertThat(order.total()).isEqualByComparingTo("194.00");
        assertThat(order.items()).extracting(OrderResponse.Item::unitPrice)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("80.00"), new BigDecimal("50.00"));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        verify(productRepository).decreaseStock(1L, 2);
        verify(productRepository).decreaseStock(2L, 1);
        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).deleteAllByUserId(USER_ID);
    }

    @Test
    void placeOrder_whenStockRunsOut_throwsAndSavesNothing() {
        Product phone = product(1L, "Phone", "100.00", null, 1);
        givenCart(new CartItem(user, phone, 1));
        givenAddressAndShipping();
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(productRepository.decreaseStock(1L, 1)).thenReturn(0);

        assertThatThrownBy(() -> orderService.placeOrder(USER_ID, new PlaceOrderRequest(ADDRESS_ID, SHIPPING_ID, null)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);
        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteAllByUserId(any());
    }

    @Test
    void placeOrder_withEmptyCart_throwsCartEmpty() {
        when(cartItemRepository.findAllWithProductByUserId(USER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(USER_ID, new PlaceOrderRequest(ADDRESS_ID, SHIPPING_ID, null)))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_EMPTY);
    }

    @Test
    void placeOrder_withExpiredCoupon_throwsCouponExpired() {
        givenCart(new CartItem(user, product(1L, "Phone", "100.00", null, 5), 1));
        givenAddressAndShipping();
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        when(couponRepository.findByCode("OLD"))
                .thenReturn(Optional.of(new Coupon("OLD", DiscountType.FIXED, new BigDecimal("5"), yesterday)));

        assertThatThrownBy(() -> orderService.placeOrder(USER_ID, new PlaceOrderRequest(ADDRESS_ID, SHIPPING_ID, "old")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_EXPIRED);
        verify(productRepository, never()).decreaseStock(anyLong(), anyInt());
    }

    @Test
    void cancelMyOrder_whenPaid_restoresStockAndRefunds() {
        Order order = paidOrderWith(product(1L, "Phone", "100.00", null, 5), 2);
        when(orderRepository.findWithItemsById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelMyOrder(USER_ID, 7L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(productRepository).increaseStock(1L, 2);
    }

    @Test
    void getMyOrder_ofAnotherUser_throwsNotFound() {
        Order order = paidOrderWith(product(1L, "Phone", "100.00", null, 5), 1);
        when(orderRepository.findWithItemsById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getMyOrder(2L, 7L)).isInstanceOf(DomainException.class);
    }

    @Test
    void shipping_beforePayment_isRejected() {
        Order order = Order.place("MB-TEST", user, address, standardShipping);

        assertThatThrownBy(() -> order.ship("TRK-1"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS);
    }

    private void givenCart(CartItem... items) {
        when(cartItemRepository.findAllWithProductByUserId(USER_ID)).thenReturn(List.of(items));
    }

    private void givenAddressAndShipping() {
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
        when(shippingMethodRepository.findById(SHIPPING_ID)).thenReturn(Optional.of(standardShipping));
    }

    private Order paidOrderWith(Product product, int quantity) {
        Order order = Order.place("MB-TEST", user, address, standardShipping);
        order.addItem(product, product.getEffectivePrice(), quantity);
        order.calculateTotals(null);
        order.markPaid();
        return order;
    }

    private static Product product(Long id, String name, String price, String discountPrice, int stock) {
        var request = new ProductRequest("SKU-" + id, name, new BigDecimal(price),
                discountPrice == null ? null : new BigDecimal(discountPrice), stock, 1L, null, null, null);
        Product product = Product.create(request, new Category("Test", null), null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
