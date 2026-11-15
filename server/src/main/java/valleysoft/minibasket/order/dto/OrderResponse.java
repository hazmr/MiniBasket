package valleysoft.minibasket.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import valleysoft.minibasket.order.entity.OrderStatus;
import valleysoft.minibasket.order.entity.PaymentStatus;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        PaymentStatus paymentStatus,
        List<Item> items,
        BigDecimal subtotal,
        String couponCode,
        BigDecimal discountAmount,
        String shippingMethod,
        BigDecimal shippingFee,
        BigDecimal total,
        String shippingStreet,
        String shippingCity,
        String trackingNumber,
        Instant createdAt) {

    public record Item(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }
}
