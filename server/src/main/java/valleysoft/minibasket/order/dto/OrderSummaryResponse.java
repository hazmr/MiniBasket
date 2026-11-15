package valleysoft.minibasket.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import valleysoft.minibasket.order.entity.OrderStatus;
import valleysoft.minibasket.order.entity.PaymentStatus;

public record OrderSummaryResponse(
        Long id, String orderNumber, OrderStatus status, PaymentStatus paymentStatus, BigDecimal total,
        Instant createdAt) {
}
