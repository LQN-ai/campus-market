package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.security.UserContext;
import com.campus.market.service.CartService;
import com.campus.market.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Void> add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.add(UserContext.getUserId(), productId, quantity);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.update(UserContext.getUserId(), id, quantity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(UserContext.getUserId(), id);
        return Result.success();
    }
}
