package valleysoft.minibasket.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<Item> items, int totalQuantity, BigDecimal subtotal) {

    public record Item(Long id, Long productId, String productName, BigDecimal unitPrice, int quantity,
            BigDecimal lineTotal) {
    }
}
