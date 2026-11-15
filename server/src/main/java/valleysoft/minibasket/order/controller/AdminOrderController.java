package valleysoft.minibasket.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.order.dto.OrderResponse;
import valleysoft.minibasket.order.dto.OrderSummaryResponse;
import valleysoft.minibasket.order.dto.UpdateOrderStatusRequest;
import valleysoft.minibasket.order.entity.OrderStatus;
import valleysoft.minibasket.order.service.OrderService;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public PageResponse<OrderSummaryResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.listAllOrders(status, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request);
    }
}
