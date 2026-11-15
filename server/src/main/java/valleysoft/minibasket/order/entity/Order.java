package valleysoft.minibasket.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.product.entity.Product;
import valleysoft.minibasket.user.entity.Address;
import valleysoft.minibasket.user.entity.User;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethod shippingMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal shippingFee;

    @Column(nullable = false)
    private BigDecimal total;

    private String trackingNumber;

    @Column(nullable = false)
    private String shippingStreet;

    @Column(nullable = false)
    private String shippingCity;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderItem> items = new ArrayList<>();

    public static Order place(String orderNumber, User user, Address address, ShippingMethod shippingMethod) {
        Order order = new Order();
        order.orderNumber = orderNumber;
        order.user = user;
        order.shippingMethod = shippingMethod;
        order.shippingFee = shippingMethod.getFee();
        order.shippingStreet = address.getStreet();
        order.shippingCity = address.getCity();
        order.status = OrderStatus.PENDING;
        order.paymentStatus = PaymentStatus.UNPAID;
        return order;
    }

    public void addItem(Product product, BigDecimal unitPrice, int quantity) {
        items.add(new OrderItem(this, product, unitPrice, quantity));
    }

    public void calculateTotals(Coupon coupon) {
        this.coupon = coupon;
        this.subtotal = items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.discountAmount = coupon == null ? BigDecimal.ZERO : coupon.discountFor(subtotal);
        this.total = subtotal.subtract(discountAmount).add(shippingFee);
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING || paymentStatus != PaymentStatus.UNPAID) {
            throw invalidStatus("pay");
        }
        this.paymentStatus = PaymentStatus.PAID;
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship(String trackingNumber) {
        if (status != OrderStatus.CONFIRMED) {
            throw invalidStatus("ship");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new DomainException(ErrorCode.TRACKING_NUMBER_REQUIRED);
        }
        this.trackingNumber = trackingNumber.trim();
        this.status = OrderStatus.SHIPPED;
    }

    public void markDelivered() {
        if (status != OrderStatus.SHIPPED) {
            throw invalidStatus("deliver");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw invalidStatus("cancel");
        }
        this.status = OrderStatus.CANCELLED;
        if (paymentStatus == PaymentStatus.PAID) {
            this.paymentStatus = PaymentStatus.REFUNDED;
        }
    }

    private DomainException invalidStatus(String action) {
        return new DomainException(ErrorCode.INVALID_ORDER_STATUS,
                Map.of("action", action, "orderNumber", orderNumber, "status", status.name()));
    }
}
