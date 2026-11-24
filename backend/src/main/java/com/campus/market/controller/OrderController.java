package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.Order;
import com.campus.market.security.RequireRole;
import com.campus.market.security.UserContext;
import com.campus.market.service.OrderService;
import com.campus.market.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<List<Order>> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(UserContext.getUserId(), dto));
    }

    @GetMapping
    public Result<List<OrderVO>> myOrders() {
        return Result.success(orderService.listByUser(UserContext.getUserId()));
    }

    @GetMapping("/merchant")
    @RequireRole("MERCHANT")
    public Result<List<OrderVO>> merchantOrders() {
        return Result.success(orderService.listByMerchant(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(UserContext.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/return")
    public Result<Void> requestReturn(@PathVariable Long id) {
        orderService.requestReturn(UserContext.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/return/audit")
    @RequireRole("MERCHANT")
    public Result<Void> auditReturn(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        orderService.auditReturn(UserContext.getUserId(), id, approve);
        return Result.success();
    }
}
