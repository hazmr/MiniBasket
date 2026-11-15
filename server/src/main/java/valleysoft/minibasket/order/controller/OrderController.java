package valleysoft.minibasket.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import valleysoft.minibasket.common.dto.PageResponse;
import valleysoft.minibasket.order.dto.OrderResponse;
import valleysoft.minibasket.order.dto.OrderSummaryResponse;
import valleysoft.minibasket.order.dto.PlaceOrderRequest;
import valleysoft.minibasket.order.service.OrderService;
import valleysoft.minibasket.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(principal.getId(), request);
    }

    @GetMapping
    public PageResponse<OrderSummaryResponse> listMyOrders(@AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.listMyOrders(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getMyOrder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return orderService.getMyOrder(principal.getId(), id);
    }

    @PostMapping("/{id}/pay")
    public OrderResponse payOrder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return orderService.payOrder(principal.getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return orderService.cancelMyOrder(principal.getId(), id);
    }
}
