package valleysoft.minibasket.order.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.cart.entity.CartItem;
import valleysoft.minibasket.cart.repository.CartItemRepository;
import valleysoft.minibasket.cart.service.CartService;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.order.dto.OrderResponse;
import valleysoft.minibasket.order.dto.OrderSummaryResponse;
import valleysoft.minibasket.order.dto.PlaceOrderRequest;
import valleysoft.minibasket.order.dto.UpdateOrderStatusRequest;
import valleysoft.minibasket.order.entity.Coupon;
import valleysoft.minibasket.order.entity.Order;
import valleysoft.minibasket.order.entity.OrderStatus;
import valleysoft.minibasket.order.entity.ShippingMethod;
import valleysoft.minibasket.order.mapper.OrderMapper;
import valleysoft.minibasket.order.repository.OrderRepository;
import valleysoft.minibasket.order.repository.ShippingMethodRepository;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.product.service.StockService;
import valleysoft.minibasket.user.entity.Address;
import valleysoft.minibasket.user.repository.AddressRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final CouponService couponService;
    private final StockService stockService;
    private final OrderNumberGenerator orderNumberGenerator;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        List<CartItem> cartItems = cartItemRepository.findAllWithProductByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new DomainException(ErrorCode.CART_EMPTY);
        }

        Address address = addressRepository.findByIdAndUserId(request.addressId(), userId)
                .orElseThrow(() -> new DomainException(ErrorCode.ADDRESS_NOT_FOUND, Map.of("id", request.addressId())));
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.shippingMethodId())
                .orElseThrow(() -> new DomainException(ErrorCode.SHIPPING_METHOD_NOT_FOUND, Map.of("id", request.shippingMethodId())));
        Coupon coupon = couponService.findUsableCoupon(request.couponCode()).orElse(null);

        Order order = Order.place(orderNumberGenerator.next(), userRepository.getReferenceById(userId), address, shippingMethod);
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            stockService.take(product, cartItem.getQuantity());
            order.addItem(product, product.getEffectivePrice(), cartItem.getQuantity());
        }
        order.calculateTotals(coupon);

        orderRepository.save(order);
        cartService.clearCart(userId);
        log.info("Order {} placed by user {} for {}", order.getOrderNumber(), userId, order.getTotal());
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listMyOrders(Long userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findAllByUserId(userId, pageable).map(orderMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        return orderMapper.toResponse(findOwnOrder(userId, orderId));
    }

    @Transactional
    public OrderResponse payOrder(Long userId, Long orderId) {
        Order order = findOwnOrder(userId, orderId);
        order.markPaid();
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId) {
        Order order = findOwnOrder(userId, orderId);
        cancelAndRestock(order);
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public PageResponse<OrderSummaryResponse> listAllOrders(OrderStatus status, Pageable pageable) {
        var orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findAllByStatus(status, pageable);
        return PageResponse.from(orders.map(orderMapper::toSummary));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public OrderResponse getOrder(Long orderId) {
        return orderMapper.toResponse(findOrder(orderId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ORDER_MANAGE')")
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrder(orderId);
        switch (request.status()) {
            case SHIPPED -> order.ship(request.trackingNumber());
            case DELIVERED -> order.markDelivered();
            case CANCELLED -> cancelAndRestock(order);
            default -> throw new DomainException(ErrorCode.STATUS_NOT_SETTABLE, Map.of("status", request.status().name()));
        }
        return orderMapper.toResponse(order);
    }

    private void cancelAndRestock(Order order) {
        order.cancel();
        order.getItems().forEach(item -> stockService.putBack(item.getProduct(), item.getQuantity()));
    }

    private Order findOwnOrder(Long userId, Long orderId) {
        Order order = findOrder(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new DomainException(ErrorCode.ORDER_NOT_FOUND, Map.of("id", orderId));
        }
        return order;
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findWithItemsById(orderId).orElseThrow(() -> new DomainException(ErrorCode.ORDER_NOT_FOUND, Map.of("id", orderId)));
    }
}
