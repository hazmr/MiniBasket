package valleysoft.minibasket.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import valleysoft.minibasket.order.entity.OrderStatus;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status, @Size(max = 100) String trackingNumber) {
}
