package valleysoft.minibasket.order.dto;

import java.math.BigDecimal;

public record ShippingMethodResponse(Long id, String name, BigDecimal fee) {
}
